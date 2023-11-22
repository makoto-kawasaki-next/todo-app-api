package controllers.todo

import lib.model.Todo.Id
import lib.model.{Todo, TodoCategory, TodoStatus}
import lib.persistence.onMySQL.{TodoCategoryRepository, TodoRepository}
import model.TodoFormData.form
import model.{TodoFormData, ViewValueTodo}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
  implicit val messageApi: MessagesApi
)(implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  def list(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    val todosFuture = TodoRepository.all()
    val categoriesFuture = TodoCategoryRepository.all()
    val futures = todosFuture.zip(categoriesFuture)
    futures.transform {
      case Success(res) =>
        val output = res._1.map(todo => {
          val categoryName = res._2.find(category => category.id == todo.v.categoryId).fold("存在しないカテゴリ")(_.v.name)
          ViewValueTodo(todo.id, categoryName, todo.v.title, todo.v.body, todo.v.state.name)
        })
        Success(Ok(views.html.todo.list(output)))
      case Failure(_) => Success(NotFound)
    }
  }

  def add(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    for {
      categories <- TodoCategoryRepository.all()
    } yield {
      val categoriesForSelect = categories.map(category =>
        (category.id.toString, category.v.name)
      ).toMap
      Ok(views.html.todo.add(form, categoriesForSelect))
    }
  }

  def store(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    form.bindFromRequest().fold(
      (errorForm: Form[TodoFormData]) => {
        for {
          categories <- TodoCategoryRepository.all()
        } yield {
          val categoriesForSelect = categories.map(category => (category.id.toString, category.v.name)).toMap
          BadRequest(views.html.todo.add(errorForm, categoriesForSelect))
        }
      },
      (form: TodoFormData) => {
        for {
          _ <- TodoRepository.add(Todo(TodoCategory.Id(form.categoryId), form.title, form.body, form.state))
        } yield Redirect(routes.TodoController.list())
      }
    )
  }

  def edit(id: Long): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>

    val categories = TodoCategoryRepository.all()
    val todo = TodoRepository.get(Id(id))
    todo.zip(categories).transform {
      case Success(results) =>
        results._1 match {
          case Some(todo) =>
            val formData = TodoFormData(todo.v.categoryId, todo.v.title, todo.v.body, todo.v.state)
            val categories = results._2.map(category => (category.id.toString, category.v.name)).toMap
            val status = TodoStatus.values.map(state => (state.code.toString, state.name)).toMap
            Success(Ok(views.html.todo.edit(id, form.fill(formData), categories, status)))
          case None => Success(NotFound)
        }
      case Failure(exception) => Success(NotFound)
    }
  }

  private def errorUpdate(formWithError: Form[TodoFormData]): Result = Redirect(routes.TodoController.list())

  def update(id: Long): Action[TodoFormData] = Action(parse.form(form, onErrors = errorUpdate)) async { implicit request =>
    val data = request.body
    TodoRepository.get(Id(id)).map {
      case Some(entity) =>
        val target = entity.map(_.copy(categoryId = data.categoryId, title = data.title, body = data.body, state = data.state))
        TodoRepository.update(target)
        Redirect(routes.TodoController.list()).flashing("successMessage" -> "success.update.todo")
      case None => Redirect(routes.TodoController.list()).flashing("errorMessage" -> "failure.update.todo")
    }
  }

  def delete(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    request.body.asFormUrlEncoded.get("id").headOption match {
      case None => Future.successful(NotFound)
      case Some(id) =>
        TodoRepository.remove(Id(id.toLong)).map {
          case Some(_) => Redirect(routes.TodoController.list()).flashing("successMessage" -> "success.delete.todo")
          case None => Redirect(routes.TodoController.list()).flashing("errorMessage" -> "failure.delete.todo")
        }
    }
  }
}

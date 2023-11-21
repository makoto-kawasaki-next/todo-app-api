package controllers.todo

import lib.model.Todo.Id
import lib.model.TodoStatus.BeforeExec
import lib.model.{Todo, TodoCategory, TodoStatus}
import lib.persistence.onMySQL.{TodoCategoryRepository, TodoRepository}
import model.ViewValueTodo
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, number, shortNumber}
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
)(implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  val form: Form[TodoFormData] = Form(
    mapping(
      "categoryId" -> longNumber,
      "title" -> nonEmptyText(minLength = 1),
      "body" -> nonEmptyText(minLength = 1),
      "state" -> shortNumber
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
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
          _ <- TodoRepository.add(Todo(TodoCategory.Id(form.categoryId), form.title, form.body, BeforeExec))
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
            val formData = TodoFormData(todo.v.categoryId.toInt, todo.v.title, todo.v.body, todo.v.state.code)
            val categories = results._2.map(category => (category.id.toString, category.v.name)).toMap
            val status = TodoStatus.values.map(state => (state.code.toString, state.name)).toMap
            Success(Ok(views.html.todo.edit(id, form.fill(formData), categories, status)))
          case None => Success(NotFound)
        }
      case Failure(exception) => Success(NotFound)
    }
  }

  def update(id: Long): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    form.bindFromRequest().fold(
      (formWithError: Form[TodoFormData]) => {
        TodoCategoryRepository.all().map { categoriesRes =>
          println()
          val status: Map[String, String] = TodoStatus.values.map(state => (state.code.toString, state.name)).toMap
          val categories: Map[String, String] = categoriesRes.map(category => (category.id.toString, category.v.name)).toMap
          BadRequest(views.html.todo.edit(id, formWithError, categories, status))
        }
      },
      (data: TodoFormData) => {
        TodoRepository.get(Id(id)).map {
         case Some(entity) =>
           val target = entity.map(_.copy(categoryId = TodoCategory.Id(data.categoryId), title = data.title, body = data.body, state = TodoStatus(data.state)))
           TodoRepository.update(target)
           Redirect(routes.TodoController.list())
         case None => NotFound
        }
      }
    )
  }
  def delete(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    request.body.asFormUrlEncoded.get("id").headOption match {
      case None => Future.successful(NotFound)
      case Some(id) =>
        TodoRepository.remove(Id(id.toLong)).map(opt =>
          opt match {
            case Some(_) => Redirect(routes.TodoController.list())
            case None => NotFound
          }
        )
    }
  }
}
case class TodoFormData(categoryId: Long, title: String, body: String, state: Short)

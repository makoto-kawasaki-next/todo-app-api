package controllers.todo

import lib.model.Todo.Id
import lib.model.{BeforeExec, Todo, TodoStatus}
import lib.persistence.db.TodoTable
import lib.persistence.onMySQL.{TodoCategoryRepository, TodoRepository}
import model.ViewValueTodo
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number}
import play.api.i18n.I18nSupport
import play.api.libs.typedmap.TypedKey
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
)(implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  val form: Form[TodoFormData] = Form(
    mapping(
      "categoryId" -> number,
      "title" -> nonEmptyText(minLength = 1),
      "body" -> nonEmptyText(minLength = 1),
      "state" -> number
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
  def list(): Action[AnyContent] = Action async {
    val todosFuture = TodoRepository.all()
    val categoriesFuture = TodoCategoryRepository.all()
    val futures = todosFuture.zip(categoriesFuture)
    futures.transform {
      case Success(res) =>
        val output = res._1.map(todo => {
          val categoryName = res._2.find(category => category.id == todo.v.categoryId).fold("存在しないカテゴリ")(_.v.name)
          ViewValueTodo(todo.id, categoryName, todo.v.title, todo.v.body, TodoStatus.getByCode(todo.v.state).name)
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
        println(errorForm)
        for {
          categories <- TodoCategoryRepository.all()
        } yield {
          val categoriesForSelect = categories.map(category => (category.id.toString, category.v.name)).toMap
          BadRequest(views.html.todo.add(errorForm, categoriesForSelect))
        }
      },
      (form: TodoFormData) => {
        for {
          _ <- TodoRepository.add(Todo(form.categoryId.toLong, form.title, form.body, BeforeExec))
        } yield Redirect(routes.TodoController.list())
      }
    )
  }

  def edit(id: Long): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    TodoCategoryRepository.all().map {categoriesRes =>
      val status: Map[String, String] = TodoStatus.values.map(state => (state.code.toString , state.name)).toMap
      val categories: Map[String, String] = categoriesRes.map(category => (category.id.toString, category.v.name)).toMap
      // ここでエラーが起こる。IDの生成方法がよろしくないのか？
      val todo = TodoRepository.get(Id(id))
      Ok(views.html.todo.edit(id, form, categories, status))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
//    form.bindFromRequest().fold(
//      (formWithError: Form[TodoFormData]) => {
//        TodoCategoryRepository.all().map { categoriesRes =>
//          val status: Map[String, String] = TodoStatus.values.map(state => (state.code.toString, state.name)).toMap
//          val categories: Map[String, String] = categoriesRes.map(category => (category.id.toString, category.v.name)).toMap
//          BadRequest(views.html.todo.edit(id, formWithError, categories, status))
//        }
//      },
//      (data: TodoFormData) => {
//        val todoId = TypedKey[Todo](id)
//        TodoRepository.get(todoId).map {
//         case Some(entity) =>
//           val target = entity.v.copy(categoryId = data.categoryId, title = data.title, body = data.body, state = data.state)
//           TodoRepository.update(target.toEmbeddedId)
//           Redirect(routes.TodoController.list())
//         case None => NotFound
//        }
//      }
//    )
    Redirect(routes.TodoController.list())
  }
}
case class TodoFormData(categoryId: Int, title: String, body: String, state: Int)

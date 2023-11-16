package controllers.todo

import controllers.form.TodoFormData
import controllers.output.TodoListOutput
import lib.model.Todo
import lib.persistence.{TodoCategoryRepository, TodoRepository}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import slick.jdbc.{JdbcProfile, MySQLProfile}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
)
  (implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  implicit val mySQLProfile = MySQLProfile
  val todoRepository = TodoRepository()
  val todoCategoryRepository = TodoCategoryRepository()

  def list() = Action async { implicit request: Request[AnyContent] =>
    for {
      todos <- todoRepository.all()
      categories <- todoCategoryRepository.all()
    } yield {
      val result = todos.map(todo =>
        {
          val category = categories.find(_.id.getOrElse(0) == todo.categoryId).get
          TodoListOutput(todo.id.getOrElse(0), category.name, todo.title, todo.body)
        }
      )
      Ok(views.html.todo.list(result))
    }
  }

  def add() = Action async { implicit request: Request[AnyContent] =>
    for {
      categories <- todoCategoryRepository.all()
    } yield {
      val categoriesForSelect = categories.map(category => (category.id.toString, category.name)).toMap
      Ok(views.html.todo.add(TodoFormData.form, categoriesForSelect))
    }
  }

  def store() = Action async { implicit request: Request[AnyContent] =>

    TodoFormData.form.bindFromRequest().fold(
      (errorForm: Form[TodoFormData]) => {
        for {
          categories <- todoCategoryRepository.all()
        } yield {
          val categoriesForSelect = categories.map(category => (category.id.toString, category.name)).toMap
          BadRequest(views.html.todo.add(errorForm, categoriesForSelect))
        }
      },
      (form: TodoFormData) => {
        for {
          _ <- todoRepository.add(Todo(form.categoryId.toLong, form.title, form.body, 0))
        } yield Redirect(routes.TodoController.list())
      }
    )
  }
}

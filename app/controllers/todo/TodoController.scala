package controllers.todo

import controllers.output.TodoListOutput
import lib.model.Todo
import lib.persistence.{TodoCategoryRepository, TodoRepository}
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import slick.jdbc.{JdbcProfile, MySQLProfile}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
)
  (implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  implicit val mySQLProfile = MySQLProfile
  val todoRepository = TodoRepository()
  val todoCategoryRepository = TodoCategoryRepository()

  def list() = Action async { implicit request: Request[AnyContent] =>
//    repository.add(Todo(categoryId = 1, title = "test", body = "test", state = 0))
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
}

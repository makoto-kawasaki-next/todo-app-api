package controllers.todo

import controllers.output.TodoListOutput
import lib.model.Todo
import lib.persistence.TodoRepository
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
  val repository = TodoRepository()

  def list() = Action async { implicit request: Request[AnyContent] =>
//    repository.add(Todo(categoryId = 1, title = "test", body = "test", state = 0))
    for {
      todos <- repository.all()
    } yield {
      val result = todos.map(todo => TodoListOutput(todo.id.getOrElse(0), todo.categoryId.toString, todo.title, todo.body))
      Ok(views.html.todo.list(result))
    }
  }
}

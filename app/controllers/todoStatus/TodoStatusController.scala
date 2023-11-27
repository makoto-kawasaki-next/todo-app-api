package controllers.todoStatus

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import lib.model.TodoStatus
import play.api.i18n.MessagesApi
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TodoStatusController @Inject()(
  val controllerComponents: ControllerComponents,
  implicit val messageApi: MessagesApi
)(implicit ex: ExecutionContext) extends BaseController {

  case class ViewValueTodoStatus(val id: Short, val name: String)
  object ViewValueTodoStatus {
    implicit val writers: Writes[ViewValueTodoStatus] = Json.writes[ViewValueTodoStatus]
  }

  def list(): Action[AnyContent] = Action {
    val output: Seq[ViewValueTodoStatus] = TodoStatus.values.map(state => ViewValueTodoStatus(state.code, state.name))
    Ok(Json.toJson(output))
  }
}

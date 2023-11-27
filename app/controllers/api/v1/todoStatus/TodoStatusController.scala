package controllers.api.v1.todoStatus

import lib.model.TodoStatus
import model.JsValueTodoStatus
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TodoStatusController @Inject()(
  val controllerComponents: ControllerComponents,
  implicit val messageApi: MessagesApi
)(implicit ex: ExecutionContext) extends BaseController {

  def list(): Action[AnyContent] = Action {
    val output: Seq[JsValueTodoStatus] = TodoStatus.values.map(state => JsValueTodoStatus(state.code, state.name))
    Ok(Json.toJson(output))
  }
}

package controllers.api.v1.todoCategory

import lib.persistence.onMySQL.TodoCategoryRepository
import model.JsValueTodoCategory
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class TodoCategoryController@Inject()(
  val controllerComponents: ControllerComponents,
  implicit val messageApi: MessagesApi
)(implicit ex: ExecutionContext) extends BaseController {

  def list(): Action[AnyContent] = Action async {
    TodoCategoryRepository.all().transform {
      case Success(value) =>
        val categories: Seq[JsValueTodoCategory] = value.map(category => JsValueTodoCategory(category.id.toLong, category.v.name))
        Success(Ok(Json.toJson(categories)))
      case Failure(_)     => Success(NotFound)
    }
  }
}
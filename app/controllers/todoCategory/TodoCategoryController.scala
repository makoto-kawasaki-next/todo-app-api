package controllers.todoCategory

import lib.persistence.onMySQL.TodoCategoryRepository
import play.api.i18n.MessagesApi
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class TodoCategoryController@Inject()(
  val controllerComponents: ControllerComponents,
  implicit val messageApi: MessagesApi
)(implicit ex: ExecutionContext) extends BaseController {

  case class ViewValueTodoCategory(id: Long, name: String)
  object ViewValueTodoCategory {
    implicit val writers: Writes[ViewValueTodoCategory] = Json.writes[ViewValueTodoCategory]
  }
  def list(): Action[AnyContent] = Action async {
    TodoCategoryRepository.all().transform {
      case Success(value) =>
        val categories: Seq[ViewValueTodoCategory] = value.map(category => ViewValueTodoCategory(category.id.toLong, category.v.name))
        Success(Ok(Json.toJson(categories)))
      case Failure(_)     => Success(NotFound)
    }
  }
}
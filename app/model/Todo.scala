package model

import lib.model.{Todo, TodoCategory, TodoStatus}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, shortNumber}
import play.api.libs.json.{Json, Writes}

case class JsValueTodo(id: Long, category: JsValueTodoCategory, title: String, body: String, state: JsValueTodoStatus)


object JsValueTodo {
  def apply(todo: Todo.EmbeddedId, todoCategory: TodoCategory.EmbeddedId): JsValueTodo =
    new JsValueTodo(todo.id, JsValueTodoCategory(todoCategory), todo.v.title, todo.v.body, JsValueTodoStatus(todo.v.state))
  implicit val writes: Writes[JsValueTodo] = Json.writes[JsValueTodo]
}

case class ViewValueTodo(id: Long, categoryName: String, title: String, body: String, state: String)

case class TodoFormData(categoryId: TodoCategory.Id, title: String, body: String, state: TodoStatus)
object TodoFormData {

  val form: Form[TodoFormData] = Form(
    mapping(
      "categoryId" -> longNumber.transform[TodoCategory.Id](TodoCategory.Id(_), _.longValue()),
      "title" -> nonEmptyText(minLength = 1),
      "body" -> nonEmptyText(minLength = 1),
      "state" -> shortNumber.transform[TodoStatus](TodoStatus(_), _.code)
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
}

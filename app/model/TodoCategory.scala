package model

import lib.model.TodoCategory
import play.api.libs.json.{Json, Writes}

case class JsValueTodoCategory(id: Long, name: String)

object JsValueTodoCategory {
  def apply(todoCategory: TodoCategory.EmbeddedId): JsValueTodoCategory = new JsValueTodoCategory(todoCategory.id, todoCategory.v.name)
  implicit val writers: Writes[JsValueTodoCategory] = Json.writes[JsValueTodoCategory]
}
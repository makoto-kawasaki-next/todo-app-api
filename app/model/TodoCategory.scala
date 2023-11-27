package model

import play.api.libs.json.{Json, Writes}

case class JsValueTodoCategory(id: Long, name: String)

object JsValueTodoCategory {
  implicit val writers: Writes[JsValueTodoCategory] = Json.writes[JsValueTodoCategory]
}
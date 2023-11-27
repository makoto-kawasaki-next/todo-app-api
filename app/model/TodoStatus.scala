package model

import play.api.libs.json.{Json, Writes}

case class JsValueTodoStatus(val code: Short, val name: String)

object JsValueTodoStatus {
  implicit val writers: Writes[JsValueTodoStatus] = Json.writes[JsValueTodoStatus]
}

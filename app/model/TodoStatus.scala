package model

import lib.model.TodoStatus
import play.api.libs.json.{Json, Writes}

case class JsValueTodoStatus(code: Short, name: String)

object JsValueTodoStatus {
  def apply(status: TodoStatus): JsValueTodoStatus = new JsValueTodoStatus(status.code, status.name)
  implicit val writers: Writes[JsValueTodoStatus] = Json.writes[JsValueTodoStatus]
}

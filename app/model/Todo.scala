package model

import lib.model.{TodoCategory, TodoStatus}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, shortNumber}

case class ViewValueTodo(id: Long, categoryName: String, title: String, body: String, status: String)

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

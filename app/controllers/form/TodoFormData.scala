package controllers.form

import play.api.data.Form
import play.api.data.Forms._

class TodoFormData(val categoryId: Int, val title: String, val body: String) {

}

object TodoFormData {
  def apply(categoryId: Option[Int], title: String, body: String): TodoFormData = {
    println("applyが呼ばれてるよ")
    new TodoFormData(categoryId.getOrElse(0), title, body)
  }

  def unapply(form: TodoFormData): Option[(Option[Int], String, String)] = {
    Option((Option(form.categoryId), form.title, form.body))
  }

  val form = Form(
    // html formのnameがcontentのものを140文字以下の必須文字列に設定する
    mapping(
      "categoryId" -> optional(number),
      "title" -> nonEmptyText(),
      "body" -> nonEmptyText()
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
}
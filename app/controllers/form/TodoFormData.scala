package controllers.form

import play.api.data.Form
import play.api.data.Forms._

class TodoFormData(val categoryId: Int, val title: String, val body: String)

object TodoFormData {
  def apply(categoryIdStr: String, title: String, body: String): TodoFormData = {
    println("applyが呼ばれてるよ")
    val res = "[0-9]+".r findFirstIn categoryIdStr

    new TodoFormData(res.get.toInt, title, body)
  }

  def unapply(form: TodoFormData): Option[(String, String, String)] = {
    Option(form.categoryId.toString, form.title, form.body)
  }

  val form = Form(
    // html formのnameがcontentのものを140文字以下の必須文字列に設定する
    mapping(
      "categoryId" -> nonEmptyText(),
      "title" -> nonEmptyText(),
      "body" -> nonEmptyText()
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
}
package controllers.form

import play.api.data.Form
import play.api.data.Forms._

case class TodoFormData(val categoryId: Int, val title: String, val body: String)

object TodoFormData {
  val form = Form(
    // html formのnameがcontentのものを140文字以下の必須文字列に設定する
    mapping(
      "categoryId" -> number,
      "title" -> nonEmptyText(),
      "body" -> nonEmptyText()
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
}
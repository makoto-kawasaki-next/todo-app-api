package controllers.todo

import lib.model.Todo
import lib.persistence.onMySQL.{TodoCategoryRepository, TodoRepository}
import model.ViewValueTodo
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
)(implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  val form: Form[TodoFormData] = Form(
    mapping(
      "categoryId" -> number,
      "title" -> nonEmptyText(minLength = 1),
      "body" -> nonEmptyText(minLength = 1)
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
  def list(): Action[AnyContent] = Action async {
    val todosFuture = TodoRepository.all()
    val categoriesFuture = TodoCategoryRepository.all()
    val futures = todosFuture.zip(categoriesFuture)
    futures.transform {
      case Success(res) =>
        val output = res._1.map(todo => {
          val categoryName = res._2.find(category => category.id == todo.v.categoryId).fold("存在しないカテゴリ")(_.v.name)
          ViewValueTodo(todo.id, categoryName, todo.v.title, todo.v.body)
        })
        Success(Ok(views.html.todo.list(output)))
      case Failure(_) => Success(NotFound)
    }
  }

  def add(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    for {
      categories <- TodoCategoryRepository.all()
    } yield {
      val categoriesForSelect = categories.map(category =>
        (category.id.toString, category.v.name)
      ).toMap
      Ok(views.html.todo.add(form, categoriesForSelect))
    }
  }

  def store(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    form.bindFromRequest().fold(
      (errorForm: Form[TodoFormData]) => {
        for {
          categories <- TodoCategoryRepository.all()
        } yield {
          val categoriesForSelect = categories.map(category => (category.id.toString, category.v.name)).toMap
          BadRequest(views.html.todo.add(errorForm, categoriesForSelect))
        }
      },
      (form: TodoFormData) => {
        for {
          _ <- TodoRepository.add(Todo(form.categoryId.toLong, form.title, form.body, 0))
        } yield Redirect(routes.TodoController.list())
      }
    )
  }
}
case class TodoFormData(categoryId: Int, title: String, body: String)

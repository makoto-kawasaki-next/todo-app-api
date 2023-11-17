package controllers.todo

import lib.persistence.onMySQL.{TodoCategoryRepository, TodoRepository}
import model.ViewValueTodo
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
)(implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  def list() = Action async { implicit request: Request[AnyContent] =>
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

  def add() = Action async { implicit request: Request[AnyContent] =>
    for {
      categories <- TodoCategoryRepository.all()
    } yield {
      val categoriesForSelect = categories.map(category => (category.id.get.toString, category.name)).toMap
      Ok(views.html.todo.add(TodoFormData.form, categoriesForSelect))
    }
  }

  def store() = Action async { implicit request: Request[AnyContent] =>

    TodoFormData.form.bindFromRequest().fold(
      (errorForm: Form[TodoFormData]) => {
        for {
          categories <- TodoCategoryRepository.all()
        } yield {
          val categoriesForSelect = categories.map(category => (category.id.get.toString, category.name)).toMap
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

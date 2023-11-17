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
}

package controllers.todo

import lib.model.Todo
import lib.persistence.{TodoCategoryRepository, TodoRepository}
import model.ViewValueTodo
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import slick.jdbc.{JdbcProfile, MySQLProfile}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
)
  (implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  implicit val mySQLProfile = MySQLProfile
  val todoRepository = TodoRepository()
  val todoCategoryRepository = TodoCategoryRepository()

  def list() = Action { implicit request: Request[AnyContent] =>
    val todosFuture = todoRepository.all()
    val categoriesFuture = todoCategoryRepository.all()
    val futures = todosFuture.zip(categoriesFuture)
    val result = Await.ready(futures, Duration.Inf)
    result.value.get match {
      case Success(value) =>
        val output = value._1.map(todo => {
          val categoryName = value._2.find(category => category.id.get == todo.categoryId).fold("存在しないカテゴリ")(_.name)
          ViewValueTodo(todo.id.getOrElse(0), categoryName, todo.title, todo.body)
        })
        Ok(views.html.todo.list(output))
      case Failure(_)    =>
        NotFound
    }
  }
}

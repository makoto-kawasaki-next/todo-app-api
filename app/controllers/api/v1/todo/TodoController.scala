package controllers.api.v1.todo

import lib.model.Todo.Id
import lib.model.TodoStatus.BeforeExec
import lib.model.{Todo, TodoCategory, TodoStatus}
import lib.persistence.onMySQL.{TodoCategoryRepository, TodoRepository}
import model.TodoFormData.form
import model.{TodoFormData, ViewValueTodo}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
  implicit val messageApi: MessagesApi
)(implicit ex: ExecutionContext) extends BaseController with I18nSupport {

  def list(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    val todosFuture = TodoRepository.all()
    val categoriesFuture = TodoCategoryRepository.all()
    val futures = todosFuture.zip(categoriesFuture)
    futures.transform {
      case Success(res) =>
        val output = res._1.map(todo => {
          val categoryName = res._2.find(category => category.id == todo.v.categoryId).fold("存在しないカテゴリ")(_.v.name)
          ViewValueTodo(todo.id, categoryName, todo.v.title, todo.v.body, todo.v.state.name)
        })

        Success(Ok(Json.toJson(output)))
      case Failure(_) => Success(NotFound)
    }
  }

  def store(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    form.bindFromRequest().fold(
      (errorForm: Form[TodoFormData]) => {
        for {
          categories <- TodoCategoryRepository.all()
        } yield {
          val categoriesForSelect = categories.map(category => (category.id.toString, category.v.name)).toMap
          BadRequest(Json.toJson("失敗しました"))
        }
      },
      (form: TodoFormData) => {
        for {
          _ <- TodoRepository.add(Todo(TodoCategory.Id(form.categoryId), form.title, form.body, BeforeExec))
        } yield Ok(Json.toJson("成功しました"))
      }
    )
  }
}

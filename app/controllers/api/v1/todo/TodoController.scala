package controllers.api.v1.todo

import lib.model.Todo.Id
import lib.model.TodoStatus.BeforeExec
import lib.model.{Todo, TodoCategory}
import lib.persistence.onMySQL.{TodoCategoryRepository, TodoRepository}
import model.{JsValueTodo, TodoFormData}
import model.TodoFormData.form
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
        val output = for {
          todo     <- res._1
          category <- res._2.find(category => category.id == todo.v.categoryId)
        } yield {
          JsValueTodo(todo, category)
        }
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

  def get(id: Long): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    for {
      todoOpt  <- TodoRepository.get(Id(id))
      categoryOpt <- todoOpt match {
        case Some(todo) => TodoCategoryRepository.get(todo.v.categoryId)
        case None       => Future.successful(None)
      }
    } yield {
      todoOpt.fold(NotFound(Json.toJson("Todoが取得できませんでした")))(todo => {
        categoryOpt.fold(InternalServerError(Json.toJson("カテゴリが取得できませんでした")))(category => {
          Ok(Json.toJson(JsValueTodo(todo, category)))
        })
      })
    }
  }

  def update(id: Long): Action[AnyContent] = Action async { implicit request =>
    form.bindFromRequest().fold(
      (_: Form[TodoFormData]) => {
        Future.successful(BadRequest(Json.toJson("失敗しました")))
      },
      (form: TodoFormData) => {
        TodoRepository.get(Id(id)).map {
          case Some(entity) =>
            val target = entity.map(_.copy(categoryId = form.categoryId, title = form.title, body = form.body, state = form.state))
            TodoRepository.update(target)
            Ok
          case None => NotFound(Json.toJson("データがありませんでした"))
        }
      }
    )
  }
}

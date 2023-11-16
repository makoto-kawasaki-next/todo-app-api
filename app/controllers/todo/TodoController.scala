package controllers.todo

import controllers.form.TodoFormData
import controllers.output.TodoListOutput
import lib.model.Todo
import lib.persistence.{TodoCategoryRepository, TodoRepository}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import slick.jdbc.{JdbcProfile, MySQLProfile}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents,
)
  (implicit ex: ExecutionContext) extends BaseController with I18nSupport {
  implicit val mySQLProfile = MySQLProfile
  val todoRepository = TodoRepository()
  val todoCategoryRepository = TodoCategoryRepository()

  def list() = Action async { implicit request: Request[AnyContent] =>
//    repository.add(Todo(categoryId = 1, title = "test", body = "test", state = 0))
    for {
      todos <- todoRepository.all()
      categories <- todoCategoryRepository.all()
    } yield {
      val result = todos.map(todo =>
        {
          val category = categories.find(_.id.getOrElse(0) == todo.categoryId).get
          TodoListOutput(todo.id.getOrElse(0), category.name, todo.title, todo.body)
        }
      )
      Ok(views.html.todo.list(result))
    }
  }

  def add() = Action async { implicit request: Request[AnyContent] =>
    for {
      categories <- todoCategoryRepository.all()
    } yield {
      val categoriesForSelect = categories.map(category => (category.id.toString, category.name)).toMap
      Ok(views.html.todo.add(TodoFormData.form, categoriesForSelect))
    }
  }

  def store() = Action async { implicit request: Request[AnyContent] =>
    TodoFormData.form.bindFromRequest().fold(
      (errorForm: Form[TodoFormData]) => {
        // 何かを選択するとここのログが以下の感じで出ている
        // Form(play.api.data.ObjectMapping3@105144b1,Map(csrfToken -> 0f660f0008b169e301a090b40539973db46f60fc-1700120318983-681d5d15c0f7ba30651d0ee1, categoryId -> Some(1), title -> test, body -> test),List(FormError(categoryId,List(error.number),List())),None)
        println(errorForm)
        Future.successful(Redirect(routes.TodoController.list()))
      },
      (form: TodoFormData) => {
        // 何も選ばないとここに来る
        // NoneはちゃんとNoneと判定されているみたい
        println("成功しているよ")
        for {
          _ <- todoRepository.add(Todo(form.categoryId.toLong , form.title, form.body, 0))
        } yield Redirect(routes.TodoController.list())
      }
    )

  }
}

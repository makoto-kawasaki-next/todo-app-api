/**
  * This is a sample of Todo Application.
  *
  */

package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
import lib.model.Todo._
case class Todo(
  id :        Option[Id],
  categoryId: TodoCategory.Id,
  title :     String,
  body :      String,
  state :     TodoStatus,
  updatedAt : LocalDateTime = NOW,
  createdAt : LocalDateTime = NOW
) extends EntityModel[Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object Todo {

  val  Id =         the[Identity[Id]]
  type Id =         Long @@ Todo
  type WithNoId   = Entity.WithNoId [Id, Todo]
  type EmbeddedId = Entity.EmbeddedId[Id, Todo]


  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(categoryId: TodoCategory.Id, title: String, body: String, state: TodoStatus): WithNoId = {
    new Entity.WithNoId(
      new Todo(
        id =          None,
        categoryId = categoryId,
        title =      title,
        body =       body,
        state =      state,
      )
    )
  }
}

sealed class TodoStatus(val code: Short, val name: String) extends EnumStatus
object TodoStatus extends EnumStatus.Of[TodoStatus] {
  case object BeforeExec extends TodoStatus(0, "着手前")
  case object Doing extends TodoStatus(1, "進行中")
  case object Done extends TodoStatus(2, "完了")
}

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
import User._
case class User(
  id:        Option[Id],
  name:      String,
  age:       Short,
  state:     Status,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object User {

  val  Id: Identity[Id] = the[Identity[Id]]
  type Id =               Long @@ User
  type WithNoId =         Entity.WithNoId [Id, User]
  type EmbeddedId =       Entity.EmbeddedId[Id, User]

  // ステータス定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
  object Status extends EnumStatus.Of[Status] {


  }

  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(name: String, age: Short, state: Status): WithNoId = {
    new Entity.WithNoId(
      new User(
        id    = None,
        name  = name,
        age   = age,
        state = state
      )
    )
  }
}
package ActorModels

import ActorModels.UserBehavior.UserChatMessage
import ActorModels.UserWebRequestBehavior.{UserWebCommand, UserWebLoginCommand, UserWebMessage, UserWebRegisterMessage, WebReplyMessage, WebReplyRegisterMessage}
import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import Tables.UserAccountTable
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.pattern.StatusReply
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import fastparse.Parsed.Success

object UserWebRequestBehavior {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[UserWebMessage], name = "UserWebMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebLoginCommand], name = "UserWebLoginCommand"),
      new JsonSubTypes.Type(value = classOf[UserWebRegisterMessage], name = "UserWebRegisterMessage"),
    ))
  sealed trait UserWebCommand
  case class UserWebMessage(message: UserWebCommand, sender: ActorRef[StatusReply[WebReplyMessage]]) extends UserWebCommand with JacksonSerializable
  case class UserWebLoginCommand(text: String) extends UserWebCommand with JacksonSerializable
  case class UserWebRegisterMessage(userName: String, pwd: String, rePwd: String) extends UserWebCommand with JacksonSerializable

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[UserWebLoginCommand], name = "UserWebLoginCommand"),
      new JsonSubTypes.Type(value = classOf[UserWebRegisterMessage], name = "UserWebRegisterMessage"),
    ))
  sealed trait WebReplyMessage
  case class WebReplyRegisterMessage(userID: String, reason: String, outcome: Boolean) extends WebReplyMessage with JacksonSerializable

  def apply(): Behavior[UserWebCommand] = {
    Behaviors.setup(context =>
      new UserWebRequestBehavior(context)
    )
  }

}

class UserWebRequestBehavior(context: ActorContext[UserWebCommand]) extends AbstractBehavior[UserWebCommand](context) {
  override def onMessage(msg: UserWebCommand): Behavior[UserWebCommand] = {
    msg match {
      case UserWebLoginCommand(text: String) =>
        Behaviors.stopped
      case UserWebMessage(message: UserWebCommand, ref: ActorRef[StatusReply[WebReplyMessage]]) =>
        message match {
          case UserWebRegisterMessage(userName, pwd, rePwd) =>
            val newID = UserAccountTable.generateNewID()
            val rst = UserAccountTable.addUser(newID, userName, pwd)
            if(rst.isSuccess) {
              ref ! StatusReply.success(WebReplyRegisterMessage(newID, "成功创建用户!", outcome = true))
            } else {
              ref ! StatusReply.error("未能成功创建用户")
            }
            Behaviors.stopped
        }
    }
  }
}

package ActorModels

import ActorModels.UserBehavior.UserChatMessage
import ActorModels.UserWebRequestBehavior.{UserWebBasicProjectInfoMessage, UserWebBasicUserInfoMessage, UserWebCommand, UserWebLoginCommand, UserWebLoginMessage, UserWebMessage, UserWebRegisterMessage, WebReplyBasicProjectInfoMessage, WebReplyBasicUserInfoMessage, WebReplyLoginMessage, WebReplyMessage, WebReplyRegisterMessage}
import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import Tables.{ProjectBasicInfo, ProjectInfoTable, UserAccountTable, UserBasicInfo}
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
      new JsonSubTypes.Type(value = classOf[UserWebLoginMessage], name = "UserWebLoginMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebBasicUserInfoMessage], name = "UserWebBasicUserInfoMessage"),
      new JsonSubTypes.Type(value = classOf[UserWebBasicProjectInfoMessage], name = "UserWebBasicProjectInfoMessage"),
    ))
  sealed trait UserWebCommand
  case class UserWebMessage(message: UserWebCommand, sender: ActorRef[StatusReply[WebReplyMessage]]) extends UserWebCommand with JacksonSerializable
  case class UserWebLoginCommand(text: String) extends UserWebCommand with JacksonSerializable
  case class UserWebRegisterMessage(userName: String, passWord: String, rePassWord: String) extends UserWebCommand with JacksonSerializable
  case class UserWebLoginMessage(userID: String, passWord: String) extends UserWebCommand with JacksonSerializable
  case class UserWebBasicUserInfoMessage(userID: String) extends UserWebCommand with JacksonSerializable
  case class UserWebBasicProjectInfoMessage(projectID: String) extends UserWebCommand with JacksonSerializable

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[WebReplyLoginMessage], name = "WebReplyLoginMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyRegisterMessage], name = "WebReplyRegisterMessage"),
      new JsonSubTypes.Type(value = classOf[WebReplyBasicUserInfoMessage], name = "WebReplyBasicUserInfoMessage"),
    ))
  sealed trait WebReplyMessage
  case class WebReplyRegisterMessage(userID: String, reason: String, outcome: Boolean) extends WebReplyMessage with JacksonSerializable
  case class WebReplyLoginMessage(reason: String, outcome: Boolean) extends WebReplyMessage with JacksonSerializable
  case class WebReplyBasicUserInfoMessage(userBasicInfo: UserBasicInfo) extends  WebReplyMessage with JacksonSerializable
  case class WebReplyBasicProjectInfoMessage(projectBasicInfo: ProjectBasicInfo) extends WebReplyMessage with JacksonSerializable

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
            val rst = UserAccountTable.addUserWithUserID(newID, userName, pwd)
            if(rst.isSuccess) {
              ref ! StatusReply.success(WebReplyRegisterMessage(newID, "成功创建用户!", outcome = true))
            } else {
              ref ! StatusReply.error("未能成功创建用户")
            }
            Behaviors.stopped
          case UserWebLoginMessage(userID, pwd) =>
            val loginState: Boolean = UserAccountTable.checkLogin(userID, pwd).get
            if(loginState) {
              ref ! StatusReply.success(WebReplyLoginMessage(reason = "成功登录！", outcome = true))
            } else {
              ref ! StatusReply.success(WebReplyLoginMessage(reason = "未成功登录", outcome = false))
            }
            Behaviors.stopped
          case UserWebBasicUserInfoMessage(userID) =>
            ref ! StatusReply.success(WebReplyBasicUserInfoMessage(UserAccountTable.getBasicUserInfo(userID).get))
            Behaviors.stopped
          case UserWebBasicProjectInfoMessage(projectID) =>
            ref ! StatusReply.success(WebReplyBasicProjectInfoMessage(ProjectInfoTable.getBasicProjectInfo(projectID).get))
            Behaviors.stopped
        }
    }
  }
}

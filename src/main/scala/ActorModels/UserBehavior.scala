package ActorModels

import ActorModels.UserBehavior._
import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

import scala.collection.concurrent.TrieMap

// 该behavior主要用于处理websocket发来的同步性质的消息
object UserBehavior {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new JsonSubTypes.Type(value = classOf[UserChatMessage], name = "UserChatMessage"),
    ))
  trait UserCommand
  case class UserChatMessage(content: String) extends UserCommand with JacksonSerializable
  case class UserNotifierMessage(notifier: ActorRef[UserCommand], userID: String) extends UserCommand with JacksonSerializable
  case object UserPushCompleteMessage extends UserCommand with JacksonSerializable
  case class UserPushFailMessage(ex: Throwable) extends UserCommand with JacksonSerializable
  val onUserPushFail: Throwable => UserPushFailMessage = (ex: Throwable) =>  UserPushFailMessage(ex)
  case object UserWsCompleteMessage extends UserCommand
  case class UserWsFailMessage(ex: Throwable) extends UserCommand with JacksonSerializable
  case class UserWsPushMessage(content: String) extends UserCommand with JacksonSerializable
  case class UserWsConvertMessage(usrCmd: UserCommand, replyTo: ActorRef[Message]) extends UserCommand with JacksonSerializable


//  trait UserChatProtocol
//  case class Init(ackTo: ActorRef[UserCommand]) extends UserChatProtocol
//  case class Message(ackTo: ActorRef[UserCommand], msg: String) extends UserChatProtocol
//  case object Complete extends UserChatProtocol
//  case class Fail(ex: Throwable) extends UserChatProtocol

  var replyToMap: TrieMap[String, ActorRef[UserCommand]] = TrieMap.empty[String, ActorRef[UserCommand]]

  def apply(): Behavior[UserCommand] = {
    Behaviors.setup(context =>
      new UserBehavior(context))
  }
}


class UserBehavior(context: ActorContext[UserCommand]) extends AbstractBehavior[UserCommand](context){
  override def onMessage(msg: UserCommand): Behavior[UserCommand] = {
    msg match {
      case UserChatMessage(content: String) =>
        context.log.info("User {} received: {}",  content)
        this
      case UserPushCompleteMessage =>
        context.log.info(s"${context.self.path} User pushed a new message")
        this
      case UserPushFailMessage(ex) =>
        context.log.warn(s"${context.self.path} User push failed!")
        this
      case UserWsFailMessage(ex) =>
        context.log.warn("User failed to deal a WS message")
        this
      case UserWsCompleteMessage =>
        context.log.info("User completed a new ws message")
        this
      // 更新消息通知者的消息
      case UserNotifierMessage(replyTo: ActorRef[UserCommand], userID: String) =>
        replyToMap.update(userID, replyTo)
        this
      case UserWsConvertMessage(msg, replyTo) =>
        msg match {
          case UserChatMessage(content: String) =>
            replyTo ! TextMessage.Strict(content)
        }
        this

    }
  }

}

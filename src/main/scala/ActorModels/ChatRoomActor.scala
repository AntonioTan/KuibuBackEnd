package ActorModels

import ActorModels.ChatRoomActor.{ChatRoomAddUserMessage, ChatRoomChatMessage, ChatRoomCommand}
import ActorModels.UserActor.{UserChatMessage, UserCommand}
import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.model.ws.Message
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}

import scala.collection.concurrent.TrieMap


object ChatRoomActor {

  trait ChatRoomCommand

  case class ChatRoomAddUserMessage(userID: String) extends ChatRoomCommand

  case class ChatRoomChatMessage(userID: String, content: String) extends ChatRoomCommand

  def apply(): Behavior[ChatRoomCommand] = {
    Behaviors.setup(context => {
      new ChatRoomBehavior(context)
    })
  }
}

class ChatRoomBehavior(context: ActorContext[ChatRoomCommand]) extends AbstractBehavior[ChatRoomCommand](context) {
  implicit val sys: ActorSystem[Nothing] = context.system
  // 定义聊天室的Flow流 利用MergeHub和BroadCastHub进行消息收集以及分发
  val (sink, source) = MergeHub.source[Message].toMat(BroadcastHub.sink[Message])(Keep.both).run
  val chatRoomFlow = Flow.fromSinkAndSource(sink, source)
  var userMap: TrieMap[String, ActorRef[UserCommand]] = TrieMap.empty[String, ActorRef[UserCommand]]

  override def onMessage(msg: ChatRoomCommand): Behavior[ChatRoomCommand] = {
    msg match {
      case ChatRoomAddUserMessage(userID: String) =>
        this.userMap.update(userID, context.spawn(UserActor(userID), userID))
        this
      case ChatRoomChatMessage(userID: String, content: String) =>
        this.userMap.getOrElseUpdate(userID, context.spawn(UserActor(userID), userID)) ! UserChatMessage(content)
        this
    }
  }

  def getChatRoomFlow: Flow[Message, Message, NotUsed] = this.chatRoomFlow

}

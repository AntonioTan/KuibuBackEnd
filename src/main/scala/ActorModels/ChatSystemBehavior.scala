package ActorModels


import ActorModels.ChatRoomActor.{ChatRoomChatMessage, ChatRoomCommand}
import ActorModels.UserBehavior.UserChatMessage
import Globals.GlobalVariables
import Impl.ChatPortalMessage
import Impl.Messages.WebAccountMessages.WebLoginMessage
import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils.system
import WSMessage.Messages.WebPrivateChatMessage
import akka.NotUsed
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import akka.stream.typed.scaladsl.ActorSource
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

import scala.collection.concurrent.TrieMap


object ChatSystemBehavior {

  //  trait ChatSystemProtocol
//  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
//  @JsonSubTypes(
//    Array(
//      new JsonSubTypes.Type(value = classOf[ChatSystemGetChatRoom], name = "ChatSystemGetChatRoom"),
//    ))
  trait ChatSystemCommand

  case class ChatSystemInit(ackTo: ActorRef[ChatSystemCommand]) extends ChatSystemCommand

  case class ChatSystemProtocolMessage(ackTo: ActorRef[ChatSystemCommand], msg: ChatSystemChatRoomAdded) extends ChatSystemCommand

  case object ChatSystemComplete extends ChatSystemCommand

  case class ChatSystemFail(ex: Throwable) extends ChatSystemCommand

  case class ChatSystemChatRoomAdded(roomID: String) extends ChatSystemCommand

  case class ChatSystemGetChatRoom(roomID: String) extends ChatSystemCommand

  case class ChatSystemChatMessage(roomID: String, userID: String, content: String) extends ChatSystemCommand

  case class ChatSystemAskingMessage(s: Message, roomID: String, replyTo: ActorRef[Message]) extends ChatSystemCommand

  var chatRoomMap: TrieMap[String, ActorRef[ChatRoomCommand]] = TrieMap.empty[String, ActorRef[ChatRoomCommand]]
  var chatRoomFlowMap: TrieMap[String, Flow[Message, Message, NotUsed]] = TrieMap.empty[String, Flow[Message, Message, NotUsed]]

  def apply(): Behavior[ChatSystemCommand] =
    Behaviors.setup { context => {
      implicit val sys: ActorSystem[SystemBehavior.SystemCommand] = system
      Behaviors.receiveMessage {
        case ChatSystemChatRoomAdded(roomID: String) =>
          context.log.info2("ChatSystem{} {}", roomID, "to add")
          println("ChatSystem get add message!", roomID)
          val (sink, source) = MergeHub.source[Message].toMat(BroadcastHub.sink[Message])(Keep.both).run
          val chatRoomFlow = Flow.fromSinkAndSource(sink, source)
          chatRoomFlowMap.getOrElseUpdate(roomID, chatRoomFlow)
          chatRoomMap.getOrElseUpdate(roomID, context.spawn(ChatRoomActor(), roomID))
          Behaviors.same

        case ChatSystemGetChatRoom(roomID: String) =>
          println("Get it")
          chatRoomMap.getOrElseUpdate(roomID, context.spawn(ChatRoomActor(), roomID))
          chatSystem(chatRoomMap)

        case ChatSystemChatMessage(roomID: String, userID: String, content: String) =>
          chatRoomMap(roomID) ! ChatRoomChatMessage(userID, content)
          chatSystem(chatRoomMap)

        case ChatSystemAskingMessage(msg: Message, roomID: String, replyTo: ActorRef[Message]) =>
          println("ChatSystem get add message!")
          var dd:String = ""
          val (sink, source) = MergeHub.source[Message].toMat(BroadcastHub.sink[Message])(Keep.both).run
          val chatRoomFlow = Flow.fromSinkAndSource(sink, source)
          chatRoomFlowMap.getOrElseUpdate(roomID, chatRoomFlow)
          chatRoomMap.getOrElseUpdate(roomID, context.spawn(ChatRoomActor(), roomID))
          println(chatRoomFlowMap.size)
          replyTo ! msg
          Behaviors.same
      }
    }
    }


  def chatSystem(chatRoomMap: TrieMap[String, ActorRef[ChatRoomCommand]]): Behavior[ChatSystemCommand] =
    Behaviors.receive { (context, message) => {
      implicit val system: ActorSystem[Nothing] = context.system
      message match {
        case ChatSystemChatRoomAdded(roomID: String) =>
          //          context.log.info2("ChatSystem{} {}", roomID, "to add")
          println("ChatSystem get add message!")
          val (sink, source) = MergeHub.source[Message].toMat(BroadcastHub.sink[Message])(Keep.both).run
          val chatRoomFlow = Flow.fromSinkAndSource(sink, source)
          chatRoomFlowMap.getOrElseUpdate(roomID, chatRoomFlow)
          chatRoomMap.getOrElseUpdate(roomID, context.spawn(ChatRoomActor(), roomID))
          Behaviors.same

        case ChatSystemGetChatRoom(roomID: String) =>
          chatRoomMap.getOrElseUpdate(roomID, context.spawn(ChatRoomActor(), roomID))
          chatSystem(chatRoomMap)

        case ChatSystemChatMessage(roomID: String, userID: String, content: String) =>
          chatRoomMap(roomID) ! ChatRoomChatMessage(userID, content)
          chatSystem(chatRoomMap)

        case ChatSystemAskingMessage(msg: Message, roomID: String, replyTo: ActorRef[Message]) =>
          println("ChatSystem get add message!")
          implicit val system: ActorSystem[Nothing] = context.system
          val (sink, source) = MergeHub.source[Message].toMat(BroadcastHub.sink[Message])(Keep.both).run
          val chatRoomFlow = Flow.fromSinkAndSource(sink, source)
          chatRoomFlowMap.getOrElseUpdate(roomID, chatRoomFlow)
          chatRoomMap.getOrElseUpdate(roomID, context.spawn(ChatRoomActor(), roomID))
          replyTo ! msg
          Behaviors.same
      }
    }
    }

  def getChatRoomFlow(roomID: String): Flow[Message, Message, NotUsed] = {
    println("tty", chatRoomFlowMap.size)
    chatRoomFlowMap(roomID)
  }

}

package ActorModels.Test

import akka.NotUsed
import akka.actor.Status.Success
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.CompletionStrategy
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}

import scala.collection.concurrent.TrieMap

class ChatSystem(implicit system: ActorSystem) {
    private val chatRooms = TrieMap.empty[String, Flow[Message, Message, NotUsed]]
    val chatRoom: ActorRef = system.actorOf(Props(new ChatRoom), "chat2")
//    val tes = system.actorSelection("akka://default/user/chat").tell("on", chatRoom)

    def newUser(): Flow[Message, Message, NotUsed] = {
        // new connection - new user actor
        val userActor = system.actorOf(Props(new User(chatRoom)))
        val incomingMessages: Sink[Message, NotUsed] =
            Flow[Message].map {
                // transform websocket message to domain message
                case TextMessage.Strict(text) => User.IncomingMessage(text)
            }.to(Sink.actorRefWithBackpressure(userActor,
                ackMessage = User.IncomingMessage,
                onInitMessage = User.UserInitialized,
                onCompleteMessage = User.UserCompleted,
                onFailureMessage = User.onErrorMessage
            ))


        val outgoingMessages: Source[Message, NotUsed] =
            Source.actorRefWithBackpressure(ackMessage = User.OutgoingMessage,
                completionMatcher = {
                case _: Success => CompletionStrategy.immediately
                },
                failureMatcher = PartialFunction.empty)
              .mapMaterializedValue { outActor =>
                  // give the user actor a way to send messages out
                  userActor ! User.Connected(outActor)
                  NotUsed
              }.map(
                // transform domain message to web socket message
                (outMsg: User.OutgoingMessage) => {
                    println("here we are")
                    TextMessage("Here is tty"+ outMsg.text)
                })

        // then combine both to a flow
        Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
    }

    def getChatRoom(room: String): Flow[Message, Message, NotUsed] = {
        chatRooms.getOrElseUpdate(room, {
            // Easy enough to use merge hub / broadcast sink to create a dynamically joinable chat room
            val (sink, source) = MergeHub.source[Message].via(newUser()).toMat(BroadcastHub.sink[Message])(Keep.both).run
            Flow.fromSinkAndSourceCoupled(sink, source)
        })
    }




}

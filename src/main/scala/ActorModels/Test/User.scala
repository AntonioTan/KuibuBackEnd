package ActorModels.Test

import akka.actor.{Actor, ActorRef}

object User {
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
  case class UserInitialized()
  case class UserCompleted()
  final case class UserFailure(ex: Throwable)
  val onErrorMessage = (ex: Throwable) => UserFailure(ex)

}

class User(chatRoom: ActorRef) extends Actor {
  import User._

  def receive = {
    case Connected(outgoing) =>
      context.become(connected(outgoing))
  }

  def connected(outgoing: ActorRef): Receive = {
    chatRoom ! ChatRoom.Join

    {
      case IncomingMessage(text) =>
        println(self.path)
        System.out.println("incoming message: ", text)
        chatRoom ! ChatRoom.ChatMessage(text)

      case ChatRoom.ChatMessage(text) =>
        outgoing ! OutgoingMessage(text)
    }
  }

}

package chat

import akka.actor._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub}

object ChatRoom {
  case object Join
  case class ChatMessage(message: String)
}

class ChatRoom(implicit system: ActorSystem) extends Actor {
  import ChatRoom._
  var users: Set[ActorRef] = Set.empty
  val (sink, source)  = MergeHub.source[Int](perProducerBufferSize = 16)
    .toMat(BroadcastHub.sink(bufferSize = 16))(Keep.both).run()
  def receive: Receive = {
    case Join =>
      users += sender()
      // we also would like to remove the user when its actor is stopped
      context.watch(sender())

    case Terminated(user) =>
      users -= user

    case msg: ChatMessage =>
      println(self.path)
      users.foreach(_ ! msg)

  }
}

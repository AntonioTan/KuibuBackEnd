package ActorModels

import ActorModels.UserActor.{UserChatMessage, UserCommand}
import akka.actor.TypedActor.self
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, Behavior}


object UserActor {
  trait UserCommand
  case class UserChatMessage(content: String) extends UserCommand

  trait UserChatProtocol
  case class Init(ackTo: ActorRef[UserCommand]) extends UserChatProtocol
  case class Message(ackTo: ActorRef[UserCommand], msg: String) extends UserChatProtocol
  case object Complete extends UserChatProtocol
  case class Fail(ex: Throwable) extends UserChatProtocol

  def apply(userID: String): Behavior[UserCommand] = {
    Behaviors.setup(context =>
      new UserActor(context, userID))
  }
}


class UserActor(context: ActorContext[UserCommand], userID: String) extends AbstractBehavior[UserCommand](context){
//  val userFlow: Flow[Message, Message, NotUsed] = flow
//   {
//
//  }
//  def apply(): Behavior[UserActorCommand] = {
//    Behaviors.setup[UserActorCommand](context => {
//      context.setLoggerName(UserActor.getClass)
//      context.log.info("Start up a new user:{}", context.self.path)
//      Behaviors.receiveMessage {
//        message => {
//          context.log.info2("User {} received: {}", context.self.path, message.toString)
//          Behaviors.same
//        }
//      }
//    })
//  }
  override def onMessage(msg: UserCommand): Behavior[UserCommand] = {
    msg match {
      case UserChatMessage(content: String) =>
        context.log.info2("User {} received: {}", userID, content)
        this

    }
  }

}

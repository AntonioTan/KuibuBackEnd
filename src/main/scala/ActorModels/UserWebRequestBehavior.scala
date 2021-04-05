package ActorModels

import ActorModels.UserWebRequestBehavior.{UserWebCommand, UserWebLoginCommand}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.pattern.StatusReply

object UserWebRequestBehavior {
  sealed trait UserWebCommand
  case class UserWebLoginCommand(text: String, sender: ActorRef[StatusReply[Boolean]]) extends UserWebCommand
  def apply(): Behavior[UserWebCommand] = {
    Behaviors.setup(context =>
      new UserWebRequestBehavior(context)
    )
  }

}

class UserWebRequestBehavior(context: ActorContext[UserWebCommand]) extends AbstractBehavior[UserWebCommand](context) {
  override def onMessage(msg: UserWebCommand): Behavior[UserWebCommand] = {
    msg match {
      case UserWebLoginCommand(text: String, sender: ActorRef[StatusReply[Boolean]]) =>
        sender ! StatusReply.success(true)
        Behaviors.stopped
    }
  }
}

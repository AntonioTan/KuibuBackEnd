package ActorModels

import ActorModels.UserWebRequestBehavior.UserWebCommand
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply

object UserWebGuardianBehavior {

  sealed trait UserWebGuardianCommand
  // 应对WebRequestActor的生成
  case class UserWebRequestGenerateMessage(sender: ActorRef[StatusReply[UserWebRequestGenerateResponse]]) extends UserWebGuardianCommand

  case class UserWebRequestGenerateResponse(newRequestActor: ActorRef[UserWebCommand]) extends UserWebGuardianCommand

  def apply(): Behavior[UserWebGuardianCommand] = {
    Behaviors.setup(
      context =>
        Behaviors.receiveMessage {
          case UserWebRequestGenerateMessage(sender: ActorRef[StatusReply[UserWebRequestGenerateResponse]]) =>
            val newRequestActor = context.spawnAnonymous(UserWebRequestBehavior())
            sender ! StatusReply.success(UserWebRequestGenerateResponse(newRequestActor))
            Behaviors.same
        }
    )

  }

}

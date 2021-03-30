package ActorModels

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object User {

  sealed trait UserService
  case class PrivateChatService() extends UserService

  def apply(): Behavior[UserService] = {
    Behaviors.setup[UserService] {
      ctx =>
        Behaviors.receiveMessage{
          case PrivateChatService() =>
            ctx.log.info("Hello ")
            Behaviors.same
        }
    }
  }

}

package ActorModels

//import ActorModels.User.UserService
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors



object UserManager {
  sealed trait UserManagerService
  case class PrivateChatUserService() extends UserManagerService

  def apply(): Behavior[UserManagerService] = {
      Behaviors.setup {

  }
  }

}

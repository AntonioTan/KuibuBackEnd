package ActorModels

import ActorModels.ChatSystemBehavior.{ChatSystemCommand, ChatSystemGetChatRoom}
import Globals.GlobalVariables
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object SystemBehavior {

  sealed trait SystemCommand

  case class SystemCondition(condition: String) extends SystemCommand
  case class SystemStart() extends SystemCommand

  def apply(): Behavior[SystemCommand] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case SystemCondition(condition: String) => println(condition)
          context.log.info("Hello")
          Behaviors.same
        case SystemStart() =>
          GlobalVariables.chatSystem = context.spawn(ChatSystemBehavior(), "chatSystem")
          GlobalVariables.userSystem = context.spawn(UserSystemBehavior(), "userSystem")
          GlobalVariables.userWebGuardian = context.spawn(UserWebGuardianBehavior(), "userWebGuardian")
          Behaviors.same
      }
    }

  //  def apply(): Behavior[Nothing] = {
  //    Behaviors.setup {
  //      context =>
  //        Behaviors.receiveMessage { message =>
  //          Behaviors.same
  //        }
  //    }

  //  }

}

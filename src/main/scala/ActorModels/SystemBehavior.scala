package ActorModels

import ActorModels.ChatSystemActor.{ChatSystemCommand, ChatSystemGetChatRoom}
import Globals.GlobalVariables
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object SystemBehavior {
  final case class SayHello(name: String)

  def apply(): Behavior[ChatSystemCommand] =
    Behaviors.setup { context =>
      GlobalVariables.chatSystem = context.spawn(ChatSystemActor(), "ChatSystem")
      Behaviors.receiveMessage { message =>
        println("system get")
        GlobalVariables.chatSystem ! message
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

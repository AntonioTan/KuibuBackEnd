package ActorModels

import Globals.GlobalVariables
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, SpawnProtocol}

object UserWebSystem {
  def apply(): Behavior[SpawnProtocol.Command] = {
    Behaviors.setup {
      context =>
        SpawnProtocol()
    }
  }

}

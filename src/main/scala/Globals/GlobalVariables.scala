package Globals

import Plugins.MSUtils.AkkaBase
import Plugins.MSUtils.AkkaBase.AkkaClusterMessage
import Plugins.TreeObjectClusterSync.TreeObjectClusterSync
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.{ActorRef, ActorSystem}

object GlobalVariables {

  var tokenUserMap:Map[String, String] = Map()
  val watcherKey: ServiceKey[AkkaClusterMessage] =AkkaBase.qianFangTreeObjectDisplayWatcherKey

  lazy val syncActorSet: Set[ActorRef[String]] = Set(ActorSystem(TreeObjectClusterSync.apply(), "clusterSync"))
}

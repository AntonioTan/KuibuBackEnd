package Globals

import Plugins.EngineOperationAPI.{TreeObjectClusterSync, TreeObjectSyncClient}
import Plugins.MSUtils.AkkaBase
import Plugins.MSUtils.AkkaBase.AkkaClusterMessage
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.receptionist.ServiceKey

object GlobalVariables {
  /** 用于同步的akka 环境，我们这里不用它 */
  lazy val akkaSyncClient : ActorSystem[TreeObjectSyncClient.SyncCommand] = ActorSystem(TreeObjectSyncClient(), "syncClient")

  var tokenUserMap:Map[String, String] = Map()
  val watcherKey: ServiceKey[AkkaClusterMessage] =AkkaBase.qianFangEngineDisplayWatcherKey

  lazy val syncActorSet: Set[ActorRef[String]] = Set(ActorSystem(TreeObjectClusterSync.apply(), "clusterSync"))
}

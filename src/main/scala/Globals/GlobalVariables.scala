package Globals

import Plugins.CommonUtils.CommonTypes.UserPath.chosenPath
import Plugins.EngineOperationAPI.{TreeObjectClusterSync, TreeObjectSyncClient}
import Plugins.MSUtils.AkkaBase
import Plugins.MSUtils.AkkaBase.AkkaClusterMessage
import akka.actor
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.receptionist.ServiceKey
import com.typesafe.config.{Config, ConfigFactory}

object GlobalVariables {
  /** 用于同步的akka 环境，我们这里不用它 */
  lazy val akkaSyncClient : ActorSystem[TreeObjectSyncClient.SyncCommand] = ActorSystem(TreeObjectSyncClient(), "syncClient")

  var tokenUserMap:Map[String, String] = Map()
  val watcherKey: ServiceKey[AkkaClusterMessage] =AkkaBase.qianFangEngineDisplayWatcherKey

  lazy val syncActorSet: Set[ActorRef[String]] = Set(ActorSystem(TreeObjectClusterSync.apply(), "clusterSync"))
  // TODO 这里需要根据部署的ip和port进行配置
  lazy val config: Config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=6090,
      akka.remote.artery.canonical.hostname=localhost
      """)withFallback(ConfigFactory.load())
  lazy val requestAkka: actor.ActorSystem = akka.actor.ActorSystem("SingleRequest", config)
}

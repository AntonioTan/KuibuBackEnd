package Globals

import java.io.ObjectInputFilter.Config

import Plugins.MSUtils.AkkaBase
import Plugins.MSUtils.AkkaBase.AkkaClusterMessage
import Plugins.TreeObjectClusterSync.TreeObjectClusterSync
import akka.actor
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.{ActorRef, ActorSystem}
import com.typesafe.config
import com.typesafe.config.ConfigFactory

object GlobalVariables {

  var tokenUserMap:Map[String, String] = Map()
  val watcherKey: ServiceKey[AkkaClusterMessage] =AkkaBase.qianFangTreeObjectDisplayWatcherKey

  lazy val syncActorSet: Set[ActorRef[String]] = Set(ActorSystem(TreeObjectClusterSync.apply(), "clusterSync"))
  // TODO 这里需要根据部署的ip和port进行配置
  lazy val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=6090,
      akka.remote.artery.canonical.hostname=localhost
      """)withFallback(ConfigFactory.load())
  lazy val requestAkka: actor.ActorSystem = akka.actor.ActorSystem("SingleRequest", config)
}

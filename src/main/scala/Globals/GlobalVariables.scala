package Globals

import ActorModels.ChatSystemBehavior.ChatSystemCommand
import ActorModels.UserSystemBehavior.UserSystemCommand
import ActorModels.UserWebGuardianBehavior.UserWebGuardianCommand
import Plugins.MSUtils.AkkaBase
import Plugins.MSUtils.AkkaBase.AkkaClusterMessage
import akka.actor
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory

object GlobalVariables {

  var tokenUserMap:Map[String, String] = Map()
  val watcherKey: ServiceKey[AkkaClusterMessage] =AkkaBase.qianFangTreeObjectDisplayWatcherKey

  // TODO 这里需要根据部署的ip和port进行配置
  lazy val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=6090,
      akka.remote.artery.canonical.hostname=localhost
      """)withFallback(ConfigFactory.load())
  lazy val requestAkka: actor.ActorSystem = akka.actor.ActorSystem("SingleRequest", config)

  var chatSystem: ActorRef[ChatSystemCommand] = _
  var userSystem: ActorRef[UserSystemCommand] = _
  var userWebGuardian: ActorRef[UserWebGuardianCommand] = _

}

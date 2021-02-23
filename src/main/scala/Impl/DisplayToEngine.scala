package Impl

import Impl.DisplayToEngine.engineRoute
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.EngineOperationAPI.AkkaEngineOperationMessages.AkkaEngineOperationMessage
import Plugins.MSUtils.API
import Plugins.MSUtils.AkkaBase.{AkkaClusterMessage, qianFangEngineKey}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Routers}

import scala.util.Try

case class DisplayToEngineMessage(serializedInfo: String)

object DisplayToEngine {
  var engineRoute:ActorRef[AkkaClusterMessage] = _

  def setter(ctx:ActorContext[Nothing]):Unit= {
    engineRoute = ctx.spawn[AkkaClusterMessage](Routers.group(qianFangEngineKey), "engineRoute")
  }

  def send(message: AkkaEngineOperationMessage): Some[String] = {
    Some(API.sendMessage(engineRoute, IOUtils.serialize(message).get).get.info)
  }

}

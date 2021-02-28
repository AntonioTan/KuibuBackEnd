package Process

import Globals.GlobalVariables
import Plugins.EngineOperationAPI.TreeObjectSyncClient
import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils.clusterSystem
import Plugins.MSUtils.MailSender
import akka.actor.typed.ActorSystem

/** 程序入口 */
object PluginCenterEntrance {
  def main(args: Array[String]): Unit = try {
    println("=== master version (display) ===")
    println("setting up", clusterSystem)

    Thread.sleep(10000)
    GlobalVariables.akkaSyncClient = ActorSystem(TreeObjectSyncClient(), "syncClient")
    DisplayHttpServer.startHttpServer(new DisplayRoutes()(AkkaUtils.clusterSystem).displayRoutes, AkkaUtils.clusterSystem)

  } catch {
    case exception: Exception =>
      MailSender.emailException(exception)
  }
}


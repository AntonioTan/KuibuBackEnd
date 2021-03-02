package Process

import Plugins.MSUtils.AkkaBase.AkkaUtils
import Plugins.MSUtils.AkkaBase.AkkaUtils.clusterSystem
import Plugins.MSUtils.MailSender
import Utils.DBUtils

/** 程序入口 */
object PluginCenterEntrance {
  def main(args: Array[String]): Unit = try {
    println("=== master version (display) ===")
    println("setting up", clusterSystem)

    DBUtils.initDatabase()


    Thread.sleep(10000)
    DisplayHttpServer.startHttpServer(new DisplayRoutes()(AkkaUtils.clusterSystem).displayRoutes, AkkaUtils.clusterSystem)

  } catch {
    case exception: Exception =>
      MailSender.emailException(exception)
  }
}


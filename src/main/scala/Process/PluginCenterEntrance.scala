package Process

import Plugins.MSUtils.AkkaBase.AkkaUtils.clusterSystem
import Plugins.MSUtils.MailSender

/** 程序入口 */
object PluginCenterEntrance {
  def main(args: Array[String]): Unit = try {
    println("=== master version (display) ===")
    println("Hello world!")
    println("setting up", clusterSystem)
  } catch {
    case exception: Exception =>
      MailSender.emailException(exception)
  }
}


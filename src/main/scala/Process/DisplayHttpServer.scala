package Process

import Plugins.CommonUtils.CommonTypes.UserPath
import Plugins.MSUtils.MailSender
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.{Failure, Success}

object DisplayHttpServer {
  /** 搭建http服务器，监听相应端口 */
  def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext
    /** 使用routes可以使用不同的路径传输不同类型的数据 */
    val futureBinding = Http().bindAndHandle(routes, UserPath.chosenPath.setHttpServerIP(), 6060)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        println(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        println("Failed to bind HTTP endpoint, terminating system", ex)
        MailSender.emailException(ex)
        system.terminate()
    }
  }
}

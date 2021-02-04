package Process

import Impl.DisplayPortalMessage
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.MailSender
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, pathEnd, pathPrefix, post, _}
import akka.http.scaladsl.server.Route

import scala.util.{Failure, Success}

class DisplayRoutes()(implicit val system: ActorSystem[_]) {
  val displayRoutes: Route = {
    concat(
      pathPrefix("display") {
        concat {
          pathEnd {
            post {
              entity(as[String]) { bytes =>
                IOUtils.deserialize[DisplayPortalMessage](bytes) match {
                  case Success(message) =>
                    message.processResult() match {
                      case Success(value) => complete(value)
                      case Failure(exception) =>
                        MailSender.emailException(exception)
                        complete(HttpResponse(status = StatusCodes.BadRequest))
                    }
                  case Failure(exception) =>
                    exception.printStackTrace()
                    println("解码失败!!!")
                    complete(HttpResponse(status = StatusCodes.BadRequest))
                }
              }

            }
          }
        }
      }
    )
  }
}

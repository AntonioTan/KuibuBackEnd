package Process

import Impl.{ChatPortalMessage, fromObject}
import Plugins.CommonUtils.CommonExceptions.{ExceptionWithCode, MessageException}
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.{IOUtils, StringUtils}
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
                System.out.println("$ display got a post: " + bytes)
                try{
                  val message=IOUtils.deserialize[ChatPortalMessage](bytes).get
                  message.processResult() match {
                    case Success(value) =>
                      message.addMessage(returnMessage="成功", successful = true).get
                      complete(fromObject(success = true, value))
                    case Failure(exception:MessageException)=>
                      message.addMessage(exception.message, successful = false).get
                      complete(fromObject(success = true, ReplyMessage(-1, exception.message)))
                    case Failure(exception:ExceptionWithCode)=>
                      message.addMessage(exception.code, successful = false).get
                      complete(fromObject(success = true, ReplyMessage(-2, exception.getMessage)))
                    case Failure(exception: Throwable)=>
                      message.addMessage(StringUtils.exceptionToString(exception), successful = false).get
                      complete(fromObject(success = true, StringUtils.exceptionToReplyCode(exception)))
                  }
                } catch {
                  case e:Throwable=>
                    e.printStackTrace()
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

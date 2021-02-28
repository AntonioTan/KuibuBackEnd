import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}

package object Impl {
  def fromObject(success : Boolean, reply : ReplyMessage) : HttpResponse = HttpResponse(
    status = {if (success) StatusCodes.OK else StatusCodes.BadRequest},
    entity = IOUtils.serialize(reply).get
  )
}

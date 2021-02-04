import Globals.GlobalStrings
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.Encryption.Combine
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}

package object Message {

  def fromObject(success : Boolean, reply : ReplyMessage) : HttpResponse = HttpResponse(
    status = {
      if (success) StatusCodes.OK else StatusCodes.BadRequest
    },
    entity = IOUtils.serialize(reply).get
  )
  def fromObjectForDisplay(success: Boolean, reply: ReplyMessage): HttpResponse = HttpResponse(
    status = {
      if (success) StatusCodes.OK else StatusCodes.BadRequest
    },
    entity = Combine.encrypt(IOUtils.serialize(reply).get, GlobalStrings.clientPublic)
  )
  def fromString(success : Boolean, reply : String) : HttpResponse = {
    HttpResponse(
      status = {
        if (success) StatusCodes.OK else StatusCodes.BadRequest
      },
      entity = Combine.encrypt(reply, GlobalStrings.clientPublic)
    )
  }
}

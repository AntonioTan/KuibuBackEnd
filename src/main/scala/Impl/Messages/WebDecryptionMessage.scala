package Impl.Messages

import Globals.GlobalStrings
import Impl.ChatPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.Encryption.Combine

import scala.util.Try


case class WebDecryptionMessage(data: String) extends ChatPortalMessage {
  /** 定义需要采取的action */
  override def reaction(): Try[ReplyMessage] = Try {
    IOUtils.deserialize[ReplyMessage](Combine.decrypt(data, GlobalStrings.clientPrivate)).get
  }

}

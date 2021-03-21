package Impl.Messages

import Globals.GlobalStrings
import Impl.DisplayPortalMessage
import Plugins.CloudSourcingShared.Infos.RankList
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.Encryption.Combine

import scala.util.Try


case class WebDecryptionMessage(data: String) extends DisplayPortalMessage {
  /** 定义需要采取的action */
  override def reaction(): Try[ReplyMessage] = Try {
    IOUtils.deserialize[ReplyMessage](Combine.decrypt(data, GlobalStrings.clientPrivate)).get
  }

}

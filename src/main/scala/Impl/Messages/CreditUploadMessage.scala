package Impl.Messages

import Impl.DisplayPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage

import scala.util.Try

/** TODO: 把这个message重写，这个message应该放到微服务里面比较好，然后也不要和UserMessage放到一起去了，可以和JudgeMessage放到一个新的table */
case class CreditUploadMessage() extends DisplayPortalMessage {
  override def reaction(): Try[ReplyMessage] = Try(ReplyMessage(-1, "错误：CreditUploadMessage不能通过网络调用！"))
}

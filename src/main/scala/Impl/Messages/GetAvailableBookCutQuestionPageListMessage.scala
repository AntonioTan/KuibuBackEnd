package Impl.Messages

import Impl.DisplayPortalMessage
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.MSUtils.API
import Plugins.OCRAPI.GetBookPageNumMessage

import scala.collection.mutable
import scala.util.Try

case class GetAvailableBookCutQuestionPageListMessage() extends DisplayPortalMessage{
  /** 定义需要采取的action */
  override def reaction(): Try[ReplyMessage] = Try{
    val bookPageList: List[List[Int]] =  API.request[GetBookPageNumMessage, List[List[Int]]]().get
    val bookIDs: List[Int] = bookPageList.map(a => a.head).distinct
    val data = bookIDs.map(bookID => bookID -> mutable.Map(
      "page" -> List[Int](),
    )).toMap
    for (List(bookID, pageNum) <- bookPageList) {
      for (page <- Range(1, pageNum+1, 1)) {
        data(bookID)("page") ++= List(page)
      }
    }
    ReplyMessage(0, IOUtils.serialize(data).get)
  }
}

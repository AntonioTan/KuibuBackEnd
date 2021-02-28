package Impl.Messages

import java.io.ByteArrayInputStream

import Impl.DisplayPortalMessage
import Plugins.CloudSourcingAPI.AnswerMessages.GetCorrectAnswerMessage
import Plugins.CloudSourcingAPI.DisplayMessages.{CheckCorrectAnswerMessage, GetQuestionInfoByIDMessage, GetQuestionsByPageMessage}
import Plugins.CloudSourcingAPI.QuestionMessages.GetSubQuestionsByQuestionIDMessage
import Plugins.CloudSourcingShared.Answers.Answer
import Plugins.CloudSourcingShared.Answers.BookAnswers.SentenceSubAnswer
import Plugins.CloudSourcingShared.Infos.{AnswerRowClient, AnswerRowList, QuestionList}
import Plugins.CloudSourcingShared.Questions.{Question, SentenceQuestion, bookCutQuestionType, sentenceQuestionType}
import Plugins.CommonUtils.CommonTypes.ReplyMessage
import Plugins.CommonUtils.IOUtils
import Plugins.EngineOperationAPI.EngineObjectGlobals
import Plugins.EngineOperationAPI.EngineObjectGlobals.IDMap
import Plugins.EngineShared.InfoCollectionID
import Plugins.MSUtils.API
import Plugins.OCRAPI.{GetImageMessage, GetJsonInfoMessage, OCRInfoMessage}
import Plugins.UserAccountAPI.GetInfoWithUserIDMessages.GetRealNameWithUserIDMessage
import javax.imageio.ImageIO
import org.apache.commons.codec.binary.Base64
import org.joda.time.DateTime

import scala.util.Try

case class BookImageMessage(bookID: Int, page: Int) extends DisplayPortalMessage {
  def formatSentenceAnswer(ans: SentenceSubAnswer): List[Object] = {
    ans.nodeType +: ans.info.map(t => List(IDMap(t.targetID.v).getActualName) ++ t.translate.map(a => {
      EngineObjectGlobals.printCandidateObjectTrait(a)
    })
    )
  }
  /** 定义需要采取的action */
  override def reaction(): Try[ReplyMessage] = Try {
    /** 将bookID由int转为[[InfoCollectionID]] */
    val newBookID: InfoCollectionID = InfoCollectionID(bookID.toLong)
    // 获取BookCutQuestion
    val bookCutQuestion: Question = API.request[GetQuestionsByPageMessage, QuestionList](newBookID, page, bookCutQuestionType).get.l.head
    // 获取图片OCR信息
    val info = API.request[GetJsonInfoMessage, OCRInfoMessage](newBookID, page).get
    val infoDict = Map("locations" -> info.locations, "text" -> info.text)
    // 获取图片数据
    val pic = API.request[GetImageMessage, Array[Byte]](newBookID, page).get
    val sourceImg = ImageIO.read(new ByteArrayInputStream(pic))
    val w = sourceImg.getWidth()
    val h = sourceImg.getHeight()
    val base64Str = Base64.encodeBase64String(pic).trim.replaceAll("\n", "").replaceAll("\r", "")
    /** ------------------Sentence部分-----------------*/
    // 获取一页中所有的SentenceQuestion
    val sentenceQuestions: List[Question] = API.request[GetQuestionsByPageMessage, QuestionList](newBookID, page, sentenceQuestionType).get.l
    // 筛选处SentenceQuestion中有正确答案的部分并获取对应的SubSentenceQuestion
    val subSentenceQuestionMaps: Map[Question, List[Question]] = if(sentenceQuestions.isEmpty) Map[Question, List[Question]]() else sentenceQuestions.map(
       q => (q, API.request[GetSubQuestionsByQuestionIDMessage, QuestionList](q.questionID).get.l.filter(
         ques => API.request[CheckCorrectAnswerMessage, Boolean](ques.questionID).get
       ))
    ).toMap.filter(_._2.nonEmpty)
    // 根据SubSentenceQuestions将一个SentenceQuestion下的SentenceSubAnswer合并
    val sentenceData = if (subSentenceQuestionMaps.nonEmpty) subSentenceQuestionMaps.keys.map(
      sentenceQues => (sentenceQues.asInstanceOf[SentenceQuestion].text, subSentenceQuestionMaps(sentenceQues).map(
        subQues => {
          val sentenceQuestionData: List[AnswerRowClient] = API.request[GetQuestionInfoByIDMessage, AnswerRowList](subQues.questionID).get.l
          val userIDs: List[String] = sentenceQuestionData.map(r => r.userID).distinct
          subQues.questionID -> Map(
            "userName" -> userIDs.map(userID => (userID, API.request[GetRealNameWithUserIDMessage](userID).get)).toMap,
            "correctAnswer" -> formatSentenceAnswer(API.request[GetCorrectAnswerMessage, SentenceSubAnswer](subQues.questionID).get),
            "score" -> userIDs.map(userID => (userID, sentenceQuestionData.filter(d => d.userID == userID).map(_.currentScore))).toMap,
            "date" -> userIDs.map(userID => (userID, sentenceQuestionData.filter(d => d.userID == userID).map(a => new DateTime(a.submitTime).toString("yyyy-MM-dd")))).toMap,
            "userAns" -> userIDs.map(
              userID => (userID, sentenceQuestionData.filter(d => d.userID == userID).map(ans => formatSentenceAnswer(ans.content.asInstanceOf[SentenceSubAnswer])))
            ).toMap
          )
        }).toMap)).toMap
    else Map("empty" -> "yes")
    // ----------------BookCut 部分------------------
    // 获取BookCutAnswer
    val bookCutQuestionData: List[AnswerRowClient] = API.request[GetQuestionInfoByIDMessage, AnswerRowList](bookCutQuestion.questionID).get.l
    // 获取BookCutAnswer的相关信息并且将SentenceData合并组成最终的data
    val data = if (IOUtils.deserialize[Boolean](API.request[CheckCorrectAnswerMessage](bookCutQuestion.questionID).get).get)
      Map("pic" -> base64Str, "loc" -> infoDict, "width" -> w, "height" -> h,
        "empty" -> "no",
        "bookCut" -> Map(
          "userName" -> bookCutQuestionData.map(r => (r.userID, API.request[GetRealNameWithUserIDMessage](r.userID).get)).toMap,
          "correctAnswer" -> IOUtils.deserialize[Answer](API.request[GetCorrectAnswerMessage](bookCutQuestion.questionID).get).get,
          "score" -> bookCutQuestionData.map(r => (r.userID, r.currentScore.get)).toMap,
          "date" -> bookCutQuestionData.map(r => (r.userID, new DateTime(r.submitTime).toString("yyyy-MM-dd"))).toMap,
          "userAns" -> bookCutQuestionData.map(r => (r.userID, r.content)).toMap,
        ),
        "sentence" -> sentenceData
      ) else Map("pic" -> base64Str, "loc" -> infoDict, "width" -> w, "height" -> h,
      "empty" -> "yes",
      "sentence" -> sentenceData
    )
    ReplyMessage(0, IOUtils.serialize(data).get)
  }
}

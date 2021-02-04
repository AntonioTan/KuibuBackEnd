package Exceptions

import Plugins.CommonUtils.CommonExceptions.{ExceptionWithCode, ExceptionWithMessage}

/** 出错了：用户频繁登陆系统！ */
case class FrequentLoginException() extends ExceptionWithCode {
  override val code: String = "0001"
}

/** 出错了：不应该调用这个方法！ */
case class CallingWrongMethodException() extends ExceptionWithCode {
  override val code: String = "0002"
}

/** 出错了：userID不存在！ */
case class UserIDNotExistedException() extends ExceptionWithCode {
  override val code: String = "0002"
}

/** 出错了，有人尝试请求他不能请求的答案！ */
case class TryToStealAnswersException(userID: String) extends ExceptionWithCode {
  override val code: String = "0004"
  override def getMessage: String = super.getMessage + s"[${userID}]"
}



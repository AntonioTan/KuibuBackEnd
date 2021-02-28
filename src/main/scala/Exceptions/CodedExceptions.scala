package Exceptions

import Plugins.CommonUtils.CommonExceptions.ExceptionWithCode

/** 出错了：用户频繁登陆系统！ */
case class FrequentLoginException() extends ExceptionWithCode {
  override val code: String = "0001"
}


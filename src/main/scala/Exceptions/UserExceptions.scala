package Exceptions

import Plugins.CommonUtils.CommonExceptions.ExceptionWithMessage

case class UserNameExistsException() extends ExceptionWithMessage{
  override val message: String="错误：用户名已经被注册过了！"
}

case class UserIDNotExistException() extends ExceptionWithMessage {
  override val message: String="错误：用户名id不存在"
}

case class UserNamePasswordErrorException() extends ExceptionWithMessage{
  override val message: String="错误：用户名或密码错误！"
}

case class UserInvalidException() extends ExceptionWithMessage{
  override val message: String="错误：该用户已经被注销"
}

case class UserNotRegisteredForUserPassword() extends ExceptionWithMessage{
  override val message: String="错误：用户之前没有注册过用户名密码！"
}

case class UserInfoNotExistException() extends ExceptionWithMessage{
  override val message: String="错误：用户真实姓名不存在！"
}
case class UserSexNotExistException() extends ExceptionWithMessage{
  override val message: String="错误：用户性别信息不存在！"
}

case class UserTokenInvalidException() extends ExceptionWithMessage{
  override val message: String="错误：用户令牌失效/不存在，请重新登录！"
}

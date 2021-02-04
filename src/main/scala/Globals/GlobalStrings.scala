package Globals

import Plugins.CloudSourcingShared.Questions.{bookCutQuestionType, sentenceQuestionType}
import Plugins.CommonUtils.Hub.ServiceCenter.displayPortalCode
import Plugins.CommonUtils.StringUtils

object GlobalStrings {
  val serverPrivate:String="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALsd6Xk5cUhWTZ532fPKrK+MEpDM8ZVEsJk7FkNK7lZLar9RuxZ6pPZfTvNNmSdbUOHJvhTc8WmvWQuK8r6FV5xW/d/o33o1TsZeTp1eWesnjN1yVQq1BQkp+Rk84nOeusf56tlAWX42x/HMlZd4kO9qQd2E6MP3EG2VUTGnNxmDAgMBAAECgYEAqqAnEzy3OSBBnTxrCa0J9PYEw/euX9AIPWIs9eXxGc2hpJ4xvNjPbj4QrzpWaxYQ1iNx6nJizV1xFF565hKPhNh4YrXszYPBwKcRnE6sKbbZNRgKB2LU1joVlGC3ioUB2Qp6NFJBjDn31Wrh50ZCYUKO/NXcoqUGth2gNpmqbhkCQQDd7t9TDRYlaCWbB3/mBCHUhN3hgCVdIYVdbS1EEz7c4DZiDWmw06vwldBJ3720lZzwUkCLbUcsyM4MwUPBhHYFAkEA19bkqZ1guQIxKNgKaRiKT4TX9RMdf9RLZCTyjgMW8KxiuCEFZASKZkoNyzYLfYqi7M60MWijCoEUisaHW64f5wJAE+Q/HACGRaAKDtUjP4iLR7MI+Bc2QhypCXCpp/pZng5ykWNGX9T5Cj1DR/vH0xG92URmCJko0fjFjxsbaZLnyQJALOfNrShMDlHRboD8MX9kj0TBNYDjm0vM36n8OOWVqT8ETvC30WZMcRmpIwU8gMMOQJyEyt7NRQsz/eduE/n3MwJAfBEzj/n7fKmRNT2VxQTPXVZGJipynlzU76i5nHBbN1597TyMsKxGc5VVxLMPR4XQCvWNjzqGWqDAqoXe95ruiA=="
  val clientPublic:String="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCajuvgCQ+TFiGmgY/50i4qT3aqFDmS6VpEZ/cI1EfoV3OhyFjyQPEQ92oMry4Ezm119c4D1GsDoRyoW/xLLp8vAFdBMfW6AdELtT/Yl+4phcc31HmtAbQR2NTF2sCJZuZLjeyrPMKINf80b5munHTiyqnSCNV+wMt3StERwsVK7wIDAQAB"
  val serviceCode: String = displayPortalCode
  /** 工作目录 */
  val workingDir: String =System.getProperty("user.dir")
  /** 放treeObject的文件夹位置 */
  val treeObjectFolder : String = workingDir + StringUtils.slash + "tree-object" + StringUtils.slash
  val questionTypeNames=List(bookCutQuestionType, sentenceQuestionType)
}

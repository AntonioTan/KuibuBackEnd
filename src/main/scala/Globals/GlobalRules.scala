package Globals

object GlobalRules {
  /** 每次下载的问题个数 */
  val maxNumQuestionPool: Int = 5
  /** 每个页面显示的历史问题个数 */
  val historyPerPage : Int = 100
  /** sentenceAnswer超过该答案数目后显示以往答案 */
  val answersBound: Int = 2
  /** 同一个nodeType的最大答题框数，超过会提示 */
  val maxAnswersPerNodeType: Int = 8
  val maximumDailyRequest: Int = 1
}

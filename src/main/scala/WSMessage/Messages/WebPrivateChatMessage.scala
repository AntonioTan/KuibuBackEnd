package WSMessage.Messages

import WSMessage.WSMessage

case class WebPrivateChatMessage(senderID: String, receiverID: String, content: String) extends WSMessage {

}

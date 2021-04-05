package WSMessage

import Plugins.CommonUtils.CommonTypes.JacksonSerializable
import WSMessage.Messages.WebPrivateChatMessage
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[WebPrivateChatMessage], name = "WebPrivateChatMessage")
  ))
abstract class WSMessage extends JacksonSerializable{

}

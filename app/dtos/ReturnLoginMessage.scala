package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class ReturnLoginMessage(message: String, success: Boolean, token: String) {
  val jsonMessage = Json.obj(
    "message" -> message,
    "success" -> success,
    "jwt_token" -> token
  )
}

object ReturnLoginMessage {
  implicit val createReturnMessageReads: Reads[ReturnLoginMessage]   = Json.reads[ReturnLoginMessage]
  implicit val createReturnMessageWrites: Writes[ReturnLoginMessage] = Json.writes[ReturnLoginMessage]
}

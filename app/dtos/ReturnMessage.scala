package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class ReturnPlainMessage(message: String, success: Boolean) {
  val jsonMessage = Json.obj(
    "message" -> message,
    "success" -> success
  )
}

object ReturnPlainMessage {
  implicit val createReturnMessageReads: Reads[ReturnPlainMessage]   = Json.reads[ReturnPlainMessage]
  implicit val createReturnMessageWrites: Writes[ReturnPlainMessage] = Json.writes[ReturnPlainMessage]
}

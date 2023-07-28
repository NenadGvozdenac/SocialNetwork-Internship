package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class SendFriendRequest(username: String)

object SendFriendRequest {
  implicit val createSendFriendRequestReads: Reads[SendFriendRequest] = Json.reads[SendFriendRequest]
  implicit val createSendFriendRequestWrites: Writes[SendFriendRequest] = Json.writes[SendFriendRequest]
}
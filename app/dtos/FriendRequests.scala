package dtos

import models.FriendRequest
import play.api.libs.json.Json

class FriendRequests(friendRequests: Seq[FriendRequest]) {
  val jsonMessage = Json.obj("friendRequests" -> friendRequests.map(friendRequest => Json.obj(
    "usernameFrom" -> friendRequest.usernameFrom,
    "usernameTo" -> friendRequest.usernameTo,
    "status" -> friendRequest.requestStatus
      )
    )
  )
}

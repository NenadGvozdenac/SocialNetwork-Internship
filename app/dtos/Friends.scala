package dtos

import models.FriendRequest
import play.api.libs.json.Json

class Friends (friends: Seq[String]) {
  val jsonMessage = Json.obj("friends" -> friends.map(friend => Json.obj("username" -> friend)))
}
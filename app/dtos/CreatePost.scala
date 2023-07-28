package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class CreatePost(title: String, caption: String, username: String, numberOfLikes: Int) {
  val jsonMessage = Json.obj(
    "title" -> title,
    "caption" -> caption,
    "username" -> username,
    "numberOfLikes" -> numberOfLikes
  )
}

object CreatePost {
  implicit val createPostReads: Reads[CreatePost] = Json.reads[CreatePost]
  implicit val createPostWrites: Writes[CreatePost] = Json.writes[CreatePost]
}
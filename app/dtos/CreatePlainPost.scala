package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class CreatePlainPost(title: String, caption: String)

object CreatePlainPost {
  implicit val createPostReads: Reads[CreatePlainPost] = Json.reads[CreatePlainPost]
  implicit val createPostWrites: Writes[CreatePlainPost] = Json.writes[CreatePlainPost]
}
package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class PlainComment(postID: Int, commentText: String)

object PlainComment {
  implicit val createPlainCommentReads: Reads[PlainComment] = Json.reads[PlainComment]
  implicit val createPlainCommentWrites: Writes[PlainComment] = Json.writes[PlainComment]
}
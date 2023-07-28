package models

case class Comment(commentID: Int, postID: Int, userUsername: String, commentText: String) {
  def this(postID: Int, commentText: String, username: String) = this(0, postID, commentText, username)
}
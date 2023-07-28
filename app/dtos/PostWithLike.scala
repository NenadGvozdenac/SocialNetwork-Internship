package dtos

import models.{Comment, Post}

case class PostWithLike (id: Int, usernameOfPoster: String, numberOfLikes: Int, title: String, caption: String, likeByMe: Boolean, comments: Seq[Comment]) {
  def this(post: Post, likeByMe: Boolean, comments: Seq[Comment]) = this(post.id, post.usernameOfPoster, post.numberOfLikes, post.title, post.caption, likeByMe, comments)
}
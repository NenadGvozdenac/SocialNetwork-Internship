package dtos

import models.Post
import play.api.libs.json.Json

case class Posts(posts: Seq[Post]) {
  val jsonMessage = Json.obj("" +
    "friendPosts" -> posts.map(
      post => Json.obj(
        "title" -> post.title,
        "caption" -> post.caption,
        "numberOfLikes" -> post.numberOfLikes,
        "creator" -> post.usernameOfPoster,
        "id" -> post.id)))
}

case class PostsWithLikes(posts: Seq[PostWithLike]) {
  val jsonMessage = Json.obj("" +
    "friendPosts" -> posts.map(
    post => Json.obj(
      "title" -> post.title,
      "caption" -> post.caption,
      "numberOfLikes" -> post.numberOfLikes,
      "creator" -> post.usernameOfPoster,
      "likedByMe" -> post.likeByMe,
      "id" -> post.id)))
}

case class PostsWithLikesAndComments(posts: Seq[PostWithLike]) {
  val jsonMessage = Json.obj("" +
    "friendPosts" -> posts.map(
    post => Json.obj(
      "title" -> post.title,
      "caption" -> post.caption,
      "numberOfLikes" -> post.numberOfLikes,
      "creator" -> post.usernameOfPoster,
      "likedByMe" -> post.likeByMe,
      "id" -> post.id,
      "comments" -> post.comments.map(comment =>
        Json.obj("comment" -> comment.commentText, "commentID" -> comment.commentID, "commenter" -> comment.userUsername)))))
}

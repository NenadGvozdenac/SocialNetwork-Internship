package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class ReadAllPosts(listOfPosts: Seq[CreatePost]) {
  val jsonMessageWithUsername = Json.obj("posts" -> listOfPosts.map(post => Json.obj(
        "title" -> post.title,
        "caption" -> post.caption,
        "user" -> post.username,
        "numberOfLikes" -> post.numberOfLikes
      )
    )
  )

  val jsonMessageWithoutUsername = Json.obj("posts" -> Json.arr(
        listOfPosts.map(post => Json.obj(
          "title" -> post.title,
          "caption" -> post.caption
        )
      )
    )
  )
}

object ReadAllPosts {
  implicit val createReadAllPostsReads: Reads[ReadAllPosts] = Json.reads[ReadAllPosts]
  implicit val createReadAllPostsWrites: Writes[ReadAllPosts] = Json.writes[ReadAllPosts]
}
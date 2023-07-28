package repositories

import models.Post
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostRepository @Inject() (override protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  lazy val posts = TableQuery[PostTable]

  def createNewPost(post: Post): Future[Int] = {
    findLatestId().flatMap {
      case value =>
        val insertAction = posts += post
        db.run(insertAction)
    }
  }

  def findLatestId(): Future[Int] = {
    db.run(posts.sortBy(_.id.desc).map(_.id).take(1).result.headOption).map {
      case Some(value) => value
      case None => 1
    }
  }

  def readAllPosts(): Future[Seq[Post]] = {
    db.run(posts.result)
  }

  def readPostsByUsername(username: String): Future[Seq[Post]] = {
    db.run(posts.filter(_.posterUsername === username).result)
  }

  def readPostById(id: Int): Future[Option[Post]] = {
    db.run(posts.filter(_.id === id).result).map(_.headOption)
  }

  def editPost(postId: Int, newPost: Future[Post]): Future[Int] = {
    newPost.flatMap {
      case value => db.run(posts.filter(_.id === postId).map(post => post.numberOfLikes).update(value.numberOfLikes))
    }
  }

  def deletePost(id: Int): Future[Int] = {
    val deleteQuery = posts.filter(post => post.id === id).delete
    db.run(deleteQuery)
  }

  def readAllPostsInAscendingOrderFromFriends(username: String, friends: Seq[String]): Future[Seq[Post]] = {
    val friendsAndMyself: Seq[String] = friends :+ username
    val findPosts = posts.filter(_.posterUsername inSet friendsAndMyself).result
    db.run(findPosts)
  }

  class PostTable(tag: Tag) extends Table[Post](tag, "posts") {
    def id:               Rep[Int]    = column[Int]   ("PostID", O.AutoInc, O.PrimaryKey)

    def posterUsername:   Rep[String] = column[String]("PosterUsername")

    def numberOfLikes:    Rep[Int]    = column[Int]   ("NumberOfLikes")

    def title:            Rep[String] = column[String]("Title")

    def caption:          Rep[String] = column[String]("Caption")

    override def * : ProvenShape[Post] =
      (id, posterUsername, numberOfLikes, title, caption) <> (Post.tupled, Post.unapply)
  }
}

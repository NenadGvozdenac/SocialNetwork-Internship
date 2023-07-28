package repositories

import models.Like
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LikeRepository @Inject() (override protected val dbConfigProvider: DatabaseConfigProvider, postRepository: PostRepository) (implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  lazy val likes = TableQuery[LikeTable]

  def addLikeToLikes(postId: Int, username: String): Future[Int] = {
    findLatestId().flatMap {
      case value => {
        val like = Like(value, postId, username)
        val newQuery = likes += like
        db.run(newQuery)
      }
    }
  }

  def findLatestId(): Future[Int] = {
    db.run(likes.sortBy(_.likeId.desc).map(_.likeId).take(1).result.headOption).map {
      case Some(value) => value
      case None => 1
    }
  }

  def dislikePost(postId: Int, username: String): Future[Int] = {
    db.run(likes.filter(post => post.idOfPost === postId && post.usernameOfPersonThatLiked === username).delete)
  }

  def checkIfUserLikedThePost(postId: Int, username: String): Future[Option[Like]] = {
    db.run(likes.filter(likes => likes.idOfPost === postId && likes.usernameOfPersonThatLiked === username).result.map(_.headOption))
  }

  def deleteAllLikesWithPostID(id: Int) = {
    val deleteQuery = likes.filter(like => like.idOfPost === id).delete
    db.run(deleteQuery)
  }

  class LikeTable(tag: Tag) extends Table[Like](tag, "likes") {
    def likeId: Rep[Int] = column[Int]("LikeID", O.AutoInc, O.PrimaryKey)

    def idOfPost: Rep[Int] = column[Int]("IdOfPost")

    def usernameOfPersonThatLiked: Rep[String] = column[String]("PersonThatLiked")

    override def * : ProvenShape[Like] =
      (likeId, idOfPost, usernameOfPersonThatLiked) <> (Like.tupled, Like.unapply)
  }
}

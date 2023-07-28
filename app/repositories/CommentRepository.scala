package repositories

import models.Comment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommentRepository @Inject() (override protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  lazy val comments = TableQuery[CommentTable]

  def createComment(postID: Int, posterUsername: String, commentText: String): Future[Int] = {
    val newComment = new Comment(postID, posterUsername, commentText)
    val insertQuery = comments += newComment
    println(insertQuery)
    db.run(insertQuery)
  }

  def deleteComment(commentID: Int): Future[Int] = {
    val deleteQuery = comments.filter(comment => comment.commentID === commentID).delete
    db.run(deleteQuery)
  }

  def deleteAllCommentsFromUser(userUsername: String): Future[Int] = {
    val deleteQuery = comments.filter(comment => comment.userUsername === userUsername).delete
    db.run(deleteQuery)
  }

  def deleteAllCommentsWithPostID(postID: Int): Future[Int] = {
    val deleteQuery = comments.filter(comment => comment.postID === postID).delete
    db.run(deleteQuery)
  }

  def getCommentById(commentID: Int): Future[Option[Comment]] = {
    val findQuery = comments.filter(comment => comment.commentID === commentID).result.map(_.headOption)
    db.run(findQuery)
  }

  def deleteAllCommentsFromUserOnPost(userUsername: String, postID: Int): Future[Int] = {
    val deleteQuery = comments.filter(comment => comment.postID === postID && comment.userUsername === userUsername).delete
    db.run(deleteQuery)
  }

  def getCommentsForPost(postId: Int): Future[Seq[Comment]] = {
    val searchQuery = comments.filter(comment => comment.postID === postId).result
    db.run(searchQuery)
  }
  
  class CommentTable(tag: Tag) extends Table[Comment](tag, "comments") {
    def commentID: Rep[Int] = column[Int]("CommentID", O.AutoInc, O.PrimaryKey)

    def postID: Rep[Int] = column[Int]("PostID")

    def commentText: Rep[String] = column[String]("CommentText")

    def userUsername: Rep[String] = column[String]("UserUsername")

    override def * : ProvenShape[Comment] =
      (commentID, postID, userUsername, commentText) <> (Comment.tupled, Comment.unapply)
  }

}

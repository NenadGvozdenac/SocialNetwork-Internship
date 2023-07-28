package repositories

import models.FriendRequest
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendRequestRepository @Inject() (override protected val dbConfigProvider: DatabaseConfigProvider, postRepository: PostRepository) (implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  lazy val friendRequests = TableQuery[FriendRequestTable]

  def checkIfFriendRequestExists(usernameFrom: String, usernameTo: String): Future[Option[FriendRequest]] = {
    db.run(friendRequests.filter(friendRequest => friendRequest.usernameTo === usernameTo && friendRequest.usernameFrom === usernameFrom).result).map(_.headOption)
  }
  def acceptFriendRequest(usernameFrom: String, usernameTo: String): Future[Int] = {
    val acceptQuery = friendRequests.filter(friendRequest =>
      friendRequest.usernameFrom === usernameFrom &&
        friendRequest.usernameTo === usernameTo &&
        friendRequest.requestStatus === "pending").map(friendRequest => friendRequest.requestStatus).update("accepted")

    db.run(acceptQuery)
  }

  def deleteFriendRequest(usernameFrom: String, usernameTo: String): Future[Int] = {
    val deleteQuery = friendRequests.filter(friendRequest =>
      friendRequest.usernameFrom === usernameFrom &&
        friendRequest.usernameTo === usernameTo &&
        friendRequest.requestStatus === "pending").delete

    db.run(deleteQuery)
  }

  def createFriendRequest(usernameFrom: String, usernameTo: String): Future[Int] = {
    var newId = 0

    val query = friendRequests.sortBy(_.requestID.desc).map(_.requestID).take(1).result.headOption

    db.run(query).map {
      case Some(value) => newId = value
      case None => newId = 1
    }.flatMap { _ =>
      val friendRequest = FriendRequest(newId, usernameFrom, usernameTo, "pending")

      val insertAction = friendRequests += friendRequest

      db.run(insertAction)
    }
  }

  def listAllFriendRequests(): Future[Seq[FriendRequest]] = {
    db.run(friendRequests.result)
  }

  def listFriends(username: String): Future[Seq[FriendRequest]] = {
    val getAllFriends = friendRequests.filter(friendRequest =>
      friendRequest.requestStatus === "accepted" &&
      ((friendRequest.usernameFrom === username) || (friendRequest.usernameTo === username))).result

    db.run(getAllFriends)
  }
  def listAllFriends(): Future[Seq[FriendRequest]] = {
    db.run(friendRequests.filter(friendRequest => friendRequest.requestStatus === "accepted").result)
  }

  def checkIfUsersAreFriends(username: String, usernameOfPoster: String): Future[Option[FriendRequest]] = {
    db.run(friendRequests.filter(friendRequest =>
          (friendRequest.requestStatus === "accepted") &&
          ((friendRequest.usernameFrom === username && friendRequest.usernameTo === usernameOfPoster) ||
          (friendRequest.usernameTo === username && friendRequest.usernameFrom === usernameOfPoster))
    ).result).map(_.headOption)
  }


  class FriendRequestTable(tag: Tag) extends Table[FriendRequest](tag, "friend_requests") {

    def requestID: Rep[Int] = column[Int]("RequestID", O.AutoInc, O.PrimaryKey)

    def usernameFrom: Rep[String] = column[String]("UsernameFrom")

    def usernameTo: Rep[String] = column[String]("UsernameTo")

    def requestStatus: Rep[String] = column[String]("RequestStatus")

    override def * : ProvenShape[FriendRequest] =
      (requestID, usernameFrom, usernameTo, requestStatus) <> (FriendRequest.tupled, FriendRequest.unapply)
  }
}

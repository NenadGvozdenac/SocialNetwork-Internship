package services

import exceptions.{OppositeFriendRequestExistsAlreadyException, SameElementException, SameFriendRequestExistsException, UsersAlreadyFriendsException}
import models.FriendRequest
import repositories.{FriendRequestRepository, UserRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendRequestService @Inject() (repository: FriendRequestRepository, userRepository: UserRepository) (implicit ex: ExecutionContext) {

  def createFriendRequest(usernameFrom: String, usernameTo: String): Future[Int] = {
    for {
      _ <- if (usernameFrom != usernameTo) Future.unit else Future.failed(new SameElementException)

      usernameOption <- userRepository.getUserByUsername(usernameTo)
      userExists <- repository.checkIfFriendRequestExists(usernameFrom, usernameTo)

      _ <- repository.checkIfFriendRequestExists(usernameTo, usernameFrom).flatMap {
        case Some(value) => Future.failed(new OppositeFriendRequestExistsAlreadyException)
        case None => Future.unit
      }

      _ <- repository.checkIfUsersAreFriends(usernameFrom, usernameTo).flatMap {
        case Some(value) => Future.failed(new UsersAlreadyFriendsException)
        case None => Future.unit
      }

      result <- usernameOption match {
        case Some(username) =>
          if (userExists.isEmpty) {
            repository.createFriendRequest(usernameFrom, usernameTo)
          } else {
            Future.failed(new SameElementException)
          }
        case None =>
          Future.failed(new NoSuchElementException(s"User not found: $usernameTo"))
      }
    } yield result
  }

  def deleteFriendRequest(usernameFrom: String, usernameTo: String): Future[Int] = {
    checkIfFriendRequestExists(usernameFrom, usernameTo).flatMap {
      case Some(_) => repository.deleteFriendRequest(usernameFrom, usernameTo)
      case None => Future.failed(new NoSuchElementException)
    }
  }

  def acceptFriendRequest(usernameFrom: String, usernameTo: String): Future[Int] = {
    checkIfFriendRequestExists(usernameFrom, usernameTo).flatMap {
      case Some(_) => repository.acceptFriendRequest(usernameFrom, usernameTo)
      case None => Future.failed(new NoSuchElementException)
    }
  }

  def declineFriendRequest(usernameFrom: String, usernameTo: String): Future[Int] = {
    checkIfFriendRequestExists(usernameTo, usernameFrom).flatMap {
      case Some(_) => repository.deleteFriendRequest(usernameTo, usernameFrom)
      case None => Future.failed(new NoSuchElementException)
    }
  }

  def checkIfFriendRequestExists(usernameFrom: String, usernameTo: String): Future[Option[FriendRequest]]
    = repository.checkIfFriendRequestExists(usernameFrom, usernameTo)

  def checkIfUsersAreFriends(username: String, usernameOfPoster: String): Future[Option[FriendRequest]] = {
    repository.checkIfUsersAreFriends(username, usernameOfPoster)
  }

  def listFriends(username: String): Future[Seq[String]] = {
    repository.listFriends(username).map { friendRequests =>
      friendRequests.map { friendRequest =>
        if (friendRequest.usernameFrom == username) {
          friendRequest.usernameTo
        } else {
          friendRequest.usernameFrom
        }
      }
    }
  }

  def listAllFriends(): Future[Seq[FriendRequest]] = {
    repository.listAllFriends()
  }

  def listAllFriendRequests: Future[Seq[FriendRequest]]
    = repository.listAllFriendRequests()
}

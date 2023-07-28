package services

import exceptions.{PostCouldNotBeEditedException, UserAlreadyLikedPostException, UsersNotFriendsException}
import models.{Like, Post}
import repositories.{FriendRequestRepository, LikeRepository, PostRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class LikeService @Inject()(repository: LikeRepository,
                             postRepository: PostRepository,
                             friendRequestRepository: FriendRequestRepository)(implicit ex: ExecutionContext) {

  def likePost(postId: Int, username: String): Future[Int] = {
    for {
      post <- postRepository.readPostById(postId).flatMap {
        case Some(post) => Future.successful(post)
        case None => Future.failed(new NoSuchElementException("The post does not exist."))
      }
      newPost = Future.successful(post.copy(numberOfLikes = post.numberOfLikes + 1))
      usernameOfPoster = post.usernameOfPoster

      _ <- checkIfUserLikedThePost(postId, username).flatMap {
        case Some(_) => Future.failed(new UserAlreadyLikedPostException)
        case None => Future.successful(())
      }

      result <- if (username == usernameOfPoster) {
        postRepository.editPost(postId, newPost).flatMap {
          case value if value > 0 =>
            repository.addLikeToLikes(postId, username)
          case _ =>
            Future.failed(new PostCouldNotBeEditedException("Couldn't be edited."))
        }
      } else {
        for {
          _ <- friendRequestRepository.checkIfUsersAreFriends(username, usernameOfPoster).flatMap {
            case Some(_) => Future.successful(())
            case None => Future.failed(new UsersNotFriendsException)
          }
          editResult <- postRepository.editPost(postId, newPost)
          result <- editResult match {
            case value if value > 0 =>
              repository.addLikeToLikes(postId, username)
            case _ =>
              Future.failed(new PostCouldNotBeEditedException("Couldn't be edited."))
          }
        } yield result
      }
    } yield result
  }

  def dislikePost(postId: Int, username: String): Future[Option[Like]] = {
    val newPost = postRepository.readPostById(postId)
      .map(postOfId => postOfId.get)
      .map(post => Post(post.id, post.usernameOfPoster, post.numberOfLikes - 1, post.title, post.caption))

    checkIfUserLikedThePost(postId, username).andThen(like => like.map {
      case Some(_) => {
        postRepository.editPost(postId, newPost).onComplete {
          case Success(value) if value > 0 => repository.dislikePost(postId, username)
        }
      }

      case None => Future.successful(None)
    })
  }

//  def deleteAllLikesWithPostID(id: Int): Future[Int] = repository.deleteAllLikesWithPostID(id)

  def checkIfUserLikedThePost(postId: Int, username: String) = repository.checkIfUserLikedThePost(postId, username)
}

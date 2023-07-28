package services

import exceptions.{CommentBadlyFormedException, CommentNotFoundException, NotOwnCommentException, PostCouldNotBeFoundException, UserNotFoundException, UsersNotFriendsException}
import models.Comment
import repositories.CommentRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommentService @Inject() (repository: CommentRepository,
                                postService: PostService,
                                userService: RegisterService,
                                friendRequestService: FriendRequestService) (implicit ex: ExecutionContext) {

  // TODO: Check this more tomorrow
  def createComment(postID: Int, posterUsername: String, commentText: String): Future[Int] = {
    for {
      _ <- if (commentText != "") Future.unit else Future.failed(new CommentBadlyFormedException)

      personThatPostedPost <- postService.readPostById(postID)
        .flatMap {
          case Some(value) => Future.successful(value)
          case _ => Future.failed(new PostCouldNotBeFoundException)
        }

      usernameOfPersonThatPostedPost = personThatPostedPost.usernameOfPoster

      poster <- userService.getUserByUsername(posterUsername)
        .flatMap {
          case Some(value) => Future.successful(value)
          case _ => Future.failed(new UserNotFoundException)
        }

      usernamesEqual = usernameOfPersonThatPostedPost == poster.username

      _ <- if(usernamesEqual)
        Future.unit
          else
        friendRequestService.checkIfUsersAreFriends(usernameOfPersonThatPostedPost, poster.username)
        .flatMap {
          case Some(value) => Future.successful(value)
          case _ => Future.failed(new UsersNotFriendsException)
        }

      commentId <- repository.createComment(postID, posterUsername, commentText)
    } yield commentId
  }
  def deleteComment(postID: Int, commentID: Int, username: String): Future[Int] = {
    for {
      _ <- postService.readPostById(postID).flatMap {
        case Some(value) => Future.successful(value)
        case _ => Future.failed(new PostCouldNotBeFoundException)
      }

      commentValue <- getCommentById(commentID).flatMap {
        case Some(value) => Future.successful(value)
        case _ => Future.failed(new CommentNotFoundException)
      }

      _ <- if(commentValue.userUsername == username) {
        Future.unit
      } else {
        Future.failed(new NotOwnCommentException)
      }

      comment <- repository.deleteComment(commentValue.commentID)
    } yield comment
  }
  def deleteAllCommentsFromUser(userUsername: String): Future[Int] = {
    userService.getUserByUsername(userUsername).flatMap {
      case Some(value) => repository.deleteAllCommentsFromUser(userUsername)
      case _ => Future.failed(new UserNotFoundException)
    }
  }

  def deleteAllCommentsFromUserOnPost(userUsername: String, postID: Int): Future[Int] = {
    for {
      _ <- userService.getUserByUsername(userUsername).flatMap {
        case Some(value) => Future.successful(value)
        case _ => Future.failed(new UserNotFoundException)
      }

      result <- postService.readPostById(postID).flatMap {
        case Some(_) => repository.deleteAllCommentsFromUserOnPost(userUsername, postID)
        case _ => Future.failed(new PostCouldNotBeFoundException)
      }
    } yield result
  }
  def deleteAllCommentsWithPostID(postID: Int): Future[Int] = {
    postService.readPostById(postID).flatMap {
      case Some(value) => repository.deleteAllCommentsWithPostID(postID)
      case _ => Future.failed(new PostCouldNotBeFoundException)
    }
  }

  def getCommentById(commentID: Int): Future[Option[Comment]] = repository.getCommentById(commentID)
}

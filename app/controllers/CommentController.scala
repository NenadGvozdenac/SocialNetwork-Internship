package controllers

import dtos.{PlainComment, ReturnPlainMessage}
import exceptions.{CommentBadlyFormedException, CommentNotFoundException, NotOwnCommentException, PostCouldNotBeFoundException, UserNotFoundException, UsersNotFriendsException}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import requests.UserAction
import services.CommentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CommentController @Inject()(val controllerComponents: ControllerComponents,
                                  userAction: UserAction,
                                  commentService: CommentService) (implicit ex: ExecutionContext) extends BaseController {

  def addCommentToPost(): Action[PlainComment] = userAction(parse.json[PlainComment]).async { implicit userRequest =>
    val username = userRequest.username

    val postID = userRequest.body.postID
    val commentText = userRequest.body.commentText

    commentService.createComment(postID, username, commentText).map {
      case value if value > 0 => Ok(ReturnPlainMessage("Success making a comment.", true).jsonMessage)
    }.recover {
      case exception: CommentBadlyFormedException =>
        BadRequest(ReturnPlainMessage("Could not create a comment. The content of the comment mustn't be empty!", false).jsonMessage)
      case exception: PostCouldNotBeFoundException =>
        BadRequest(ReturnPlainMessage("Could not create a comment. The post could not be found!", false).jsonMessage)
      case exception: UserNotFoundException =>
        BadRequest(ReturnPlainMessage("Could not create a comment. The poster couldn't be found!", false).jsonMessage)
      case exception: UsersNotFriendsException =>
        BadRequest(ReturnPlainMessage("Could not create a comment. You aren't friends with this user!", false).jsonMessage)
      case exception: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
    }
  }

  def removeCommentOnPost(postId: Int, commentId: Int): Action[AnyContent] = userAction.async { implicit userRequest =>
    val username = userRequest.username
    commentService.deleteComment(postId, commentId, username).map {
      case value if value > 0 => Ok(ReturnPlainMessage(s"Success removing comment $commentId on $postId.", true).jsonMessage)
    }.recover {
      case exception: CommentNotFoundException =>
        BadRequest(ReturnPlainMessage("The comment could not be found.", false).jsonMessage)
      case exception: PostCouldNotBeFoundException =>
        BadRequest(ReturnPlainMessage("Post could not be found.", false).jsonMessage)
      case exception: NotOwnCommentException =>
        BadRequest(ReturnPlainMessage("Not your own comment.", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
    }
  }

  def removeAllCommentsOnPost(postId: Int) : Action[AnyContent] = userAction.async { implicit userRequest =>
    commentService.deleteAllCommentsWithPostID(postId).map {
      case value if value > 0 => Ok(ReturnPlainMessage(s"Success removing all comments from the post with id $postId.", true).jsonMessage)
    }.recover {
      case exception: PostCouldNotBeFoundException =>
        BadRequest(ReturnPlainMessage("Post could not be found.", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
    }
  }
}

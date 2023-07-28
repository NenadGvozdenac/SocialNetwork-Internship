package controllers

import dtos.ReturnPlainMessage
import exceptions.{PostCouldNotBeEditedException, UserAlreadyLikedPostException, UsersNotFriendsException}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import requests.UserAction
import services.LikeService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class LikeController @Inject()(
                       val controllerComponents: ControllerComponents,
                       likeService: LikeService,
                       userAction: UserAction)(implicit ec: ExecutionContext) extends BaseController {

  def likePost(id: Int): Action[AnyContent] = userAction.async { implicit userRequest =>
    val username = userRequest.username

    likeService.likePost(id, username).map {
      case value if value > 0 => Ok(ReturnPlainMessage("The post has been liked successfully.", true).jsonMessage)
    }.recover {
      case exception: UserAlreadyLikedPostException =>
        BadRequest(ReturnPlainMessage("The post could not be liked. You already liked this post.", false).jsonMessage)
      case exception: NoSuchElementException =>
        BadRequest(ReturnPlainMessage("The post could not be liked. The post with this ID does not exist.", false).jsonMessage)
      case exception: UsersNotFriendsException =>
        BadRequest(ReturnPlainMessage("The post could not be liked. You aren't friends with this user.", false).jsonMessage)
      case exception: PostCouldNotBeEditedException =>
        BadRequest(ReturnPlainMessage("The post could not be liked. The post could not be edited.", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
    }
  }

  def dislikePost(id: Int): Action[AnyContent] = userAction.async { implicit userRequest =>
    val username = userRequest.username

    likeService.dislikePost(id, username).map {
      case Some(value) => Ok(ReturnPlainMessage("The post has been disliked successfully.", true).jsonMessage)
      case None => BadRequest(ReturnPlainMessage("The post could not be disliked. You didn't like this post in the first place.", false).jsonMessage)
    }
  }
}

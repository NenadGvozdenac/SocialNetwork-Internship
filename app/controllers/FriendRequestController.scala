package controllers

import dtos.{FriendRequests, Friends, ReturnPlainMessage, SendFriendRequest}
import exceptions.{OppositeFriendRequestExistsAlreadyException, SameElementException, SameFriendRequestExistsException, UsersAlreadyFriendsException}
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import requests.UserAction
import services.FriendRequestService

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class FriendRequestController @Inject()(val controllerComponents: ControllerComponents,
                                        friendRequestService: FriendRequestService,
                                        userAction: UserAction
                                        )(implicit ec: ExecutionContext) extends BaseController {


  def sendFriendRequest(): Action[SendFriendRequest] = userAction(parse.json[SendFriendRequest]).async { implicit userRequest =>
    val username = userRequest.username
    val usernameTo = userRequest.body.username

    friendRequestService.createFriendRequest(username, usernameTo).map {
      case value if value > 0 => Ok(ReturnPlainMessage("The request has been sent.", true).jsonMessage)
    }.recover {
      case exception: NoSuchElementException =>
        BadRequest(ReturnPlainMessage("The friend request has not been sent. No such user exists.", false).jsonMessage)
      case exception: SameElementException =>
        BadRequest(ReturnPlainMessage("The friend request has not been sent. Same friend request already exists...", false).jsonMessage)
      case exception: SameFriendRequestExistsException =>
        BadRequest(ReturnPlainMessage("The friend request has not been sent. You can't send a friend request to yourself.", false).jsonMessage)
      case exception: UsersAlreadyFriendsException =>
        BadRequest(ReturnPlainMessage("Can't send friend request to this person. You are already friends.", false).jsonMessage)
      case exception: OppositeFriendRequestExistsAlreadyException =>
        BadRequest(ReturnPlainMessage("Can't send friend request to this person. They already sent you a friend request.", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal Server Error", false).jsonMessage)
    }
  }

  def deleteSentFriendRequest(): Action[SendFriendRequest] = userAction(parse.json[SendFriendRequest]).async { implicit userRequest =>
    val username = userRequest.username
    val usernameTo = userRequest.body.username

    friendRequestService.deleteFriendRequest(username, usernameTo).map {
      case value if value > 0 => Ok(ReturnPlainMessage("The friend request has been deleted.", true).jsonMessage)
    }.recover {
      case exception: NoSuchElementException =>
        BadRequest(ReturnPlainMessage("The friend request has not been deleted. No such friend request exists.", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal Server Error", false).jsonMessage)
    }
  }
  def acceptFriendRequest(): Action[SendFriendRequest] = userAction(parse.json[SendFriendRequest]).async { implicit userRequest =>
    val username = userRequest.username
    val usernameTo = userRequest.body.username

    friendRequestService.acceptFriendRequest(usernameTo, username).map {
      case value if value > 0 => Ok(ReturnPlainMessage("The friend request has been accepted.", true).jsonMessage)
    }.recover {
      case exception: NoSuchElementException =>
        BadRequest(ReturnPlainMessage("The friend request has not been accepted. No such friend request exists.", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal Server Error", false).jsonMessage)
    }
  }

  def declineFriendRequest(): Action[SendFriendRequest] = userAction(parse.json[SendFriendRequest]).async { implicit userRequest =>
    val username = userRequest.username
    val usernameTo = userRequest.body.username

    friendRequestService.declineFriendRequest(username, usernameTo).map {
      case value if value > 0 => Ok(ReturnPlainMessage("The friend request has been declined.", true).jsonMessage)
    }.recover {
      case exception: NoSuchElementException =>
        BadRequest(ReturnPlainMessage("The friend request has not been declined. No such friend request exists.", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal Server Error", false).jsonMessage)
    }
  }

  def listAllFriendRequests(): Action[AnyContent] = Action.async {
    friendRequestService.listAllFriendRequests.map(friendRequests => new FriendRequests(friendRequests)).map(friendRequest => Ok(friendRequest.jsonMessage))
  }

  def listFriends(): Action[AnyContent] = userAction.async { implicit userRequest =>
    val username = userRequest.username

    friendRequestService.listFriends(username).map {
      case value => Ok(new Friends(value).jsonMessage)
    }.recover {
      case _ => InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
    }
  }

  def listAllFriends(): Action[AnyContent] = Action.async {
    friendRequestService.listAllFriends().map {
      case value => Ok(new FriendRequests(value).jsonMessage)
    }
  }
}

package controllers

import dtos.{CreatePlainPost, CreatePost, ReadAllPosts, ReturnPlainMessage, Posts}
import exceptions.{PostCouldNotBeCreatedException, PostCouldNotBeEditedException, PostCouldNotBeFoundException}
import play.api.mvc.{Action, _}
import requests.UserAction
import services.PostService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PostController @Inject()(val controllerComponents: ControllerComponents,
                               postService: PostService,
                               userAction: UserAction)(implicit ec: ExecutionContext) extends BaseController {

  def createPost: Action[CreatePlainPost] = userAction(parse.json[CreatePlainPost]).async { implicit userRequest =>
    val username = userRequest.username

    val titleElement = userRequest.body.title
    val captionElement = userRequest.body.caption

    postService.createPost(titleElement, captionElement, username).map {
      case value if value > 0 => Ok(ReturnPlainMessage("Success making a post.", true).jsonMessage)
    }.recover {
      case exception: PostCouldNotBeCreatedException =>
        BadRequest(ReturnPlainMessage("Could not create post. Title and/or caption mustn't be empty..", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
    }
  }

  def readAllPosts(): Action[AnyContent] = Action.async {
    postService.readAllPosts()
      .map(posts => posts.map(post => CreatePost(post.title, post.caption, post.usernameOfPoster, post.numberOfLikes)))
      .map(posts => Ok(ReadAllPosts(posts).jsonMessageWithUsername))
  }

  def showPostForm(username: String): Action[AnyContent] = Action {
    Ok(views.html.createPost(username))
  }

  def readPostsByUsername(username: String): Action[AnyContent] = Action.async {
    postService.readPostsByUsername(username)
      .map(posts => posts.map(post => CreatePost(post.title, post.caption, post.usernameOfPoster, post.numberOfLikes)))
      .map(posts => Ok(ReadAllPosts(posts).jsonMessageWithUsername))
  }

  def readPostById(id: Int): Action[AnyContent] = Action.async {
     postService.readPostById(id).map {
       case Some(post) => Ok(CreatePost(post.title, post.caption, post.usernameOfPoster, post.numberOfLikes).jsonMessage)
       case None => BadRequest(ReturnPlainMessage("The post doesn't exist.", false).jsonMessage)
     }.recover {
       case _ => InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
     }
  }

  def deletePost(postId: Int): Action[AnyContent] = userAction.async { implicit userRequest =>
    val username = userRequest.username

    postService.deletePost(postId, username).map {
      case value if value > 0 => Ok(ReturnPlainMessage("The post has been deleted.", true).jsonMessage)
    }.recover {
      case exception: PostCouldNotBeFoundException =>
        BadRequest(ReturnPlainMessage("Post wasn't deleted. The post could not be found.", false).jsonMessage)
      case exception: PostCouldNotBeEditedException =>
        BadRequest(ReturnPlainMessage(s"Post wasn't deleted. The post could not be edited. {${exception.message}}", false).jsonMessage)
      case throwable: Throwable =>
        InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
    }
  }

  def readAllPostsFromFriends(): Action[AnyContent] = userAction.async { implicit userRequest =>
    val username = userRequest.username

    postService.readAllPostsFromFriends(username).map {
      case value => Ok(value.jsonMessage)
    }
  }
}
package requests

import models.JWTToken
import play.api.mvc.{ActionBuilder, ActionTransformer, AnyContent, BodyParsers, Request, WrappedRequest}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserRequest[A](val username: String, request: Request[A]) extends WrappedRequest[A](request)

class UserAction @Inject() (val parser: BodyParsers.Default, tokenGenerator: JWTToken)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent]
    with ActionTransformer[Request, UserRequest] {
  def transform[A](request: Request[A]): Future[UserRequest[A]] = Future.successful {
    val headers = request.headers
    val userToken = headers("Token")

    val username = tokenGenerator.retrieveData(userToken)
    new UserRequest(username, request)
  }
}
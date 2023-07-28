package controllers

import com.github.t3hnar.bcrypt._
import dtos.{CreatePerson, ReturnLoginMessage}
import models.JWTToken
import play.api.libs.json.Json
import play.api.mvc._
import services.RegisterService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class LoginController @Inject()(
                                 val controllerComponents: ControllerComponents,
                                 registerService: RegisterService,
                                 tokenGenerator: JWTToken
                               )(implicit ec: ExecutionContext) extends BaseController {

  def login: Action[AnyContent] = Action(parse.json[CreatePerson]) {
    val response = Json.obj(
      "username" -> "Login username",
      "password" -> "Login password"
    )

    Ok(response)
  }

  def loginUser: Action[CreatePerson] = Action.async(parse.json[CreatePerson]) { implicit request: Request[CreatePerson] =>
    val requestBody = request.body

    val usernameElement = requestBody.username
    val passwordElement = requestBody.password

    registerService.retrievePasswordByUsername(usernameElement).map {
      case Some(value) if passwordElement.isBcryptedBounded(value) =>
        val jwtToken = tokenGenerator.createJwtToken(usernameElement)
        Ok(ReturnLoginMessage("Login success!", true, jwtToken).jsonMessage)
      case _ => BadRequest(ReturnLoginMessage("Login not successful", false, "null").jsonMessage)
    }
  }
}
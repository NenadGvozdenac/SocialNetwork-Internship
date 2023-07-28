package controllers

import com.github.t3hnar.bcrypt._
import dtos.{CreatePerson, PeopleFromDatabase, ReturnLoginMessage, ReturnPlainMessage}
import exceptions.{UserAlreadyExistsExceptiuon, UserCouldNotBeCreatedException}
import models.JWTToken
import play.api.libs.json.Json
import play.api.mvc._
import services.RegisterService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterController @Inject()(
                                    val controllerComponents: ControllerComponents,
                                    registerService: RegisterService,
                                    tokenGenerator: JWTToken
                                  )(implicit ec: ExecutionContext) extends BaseController {

  def register: Action[AnyContent] = Action {
    val response = Json.obj(
      "username" -> "Register username",
      "password" -> "Register password"
    )

    Ok(response)
  }

  def registerUser: Action[CreatePerson] = Action.async(parse.json[CreatePerson]) { implicit request: Request[CreatePerson] =>
    val requestBody = request.body

    val usernameElement = requestBody.username
    val passwordElement = requestBody.password

    if(passwordElement == "") {
      Future.successful(BadRequest(ReturnPlainMessage("Could not create user.", false).jsonMessage))
    } else {
      try {
        val jwtToken = tokenGenerator.createJwtToken(usernameElement)
        val encryptedPassword = passwordElement.boundedBcrypt

        registerService.createUser(usernameElement, encryptedPassword).map {
          case Some(_) => Ok(ReturnLoginMessage("Registration successful!", true, jwtToken).jsonMessage)
        }.recover {
          case exception: UserAlreadyExistsExceptiuon =>
            BadRequest(ReturnPlainMessage("Registration failed. User already exists with the same username!", false).jsonMessage)
          case exception: UserCouldNotBeCreatedException =>
            BadRequest(ReturnPlainMessage("Registration failed. User could not be created. Credentials probably wrong.", false).jsonMessage)
          case throwable: Throwable =>
            InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
        }
      } catch {
        case _ => Future.successful(BadRequest(ReturnPlainMessage("Could not create user.", false).jsonMessage))
      }
    }
  }

  def readAllPeopleFromDatabase: Action[AnyContent] = Action.async {
    registerService.getAllUsers
      .map(value => PeopleFromDatabase(value.map(person => CreatePerson(person.username, person.password))))
      .map(peopleFromDatabase => Ok(peopleFromDatabase.jsonMessage))
      .recover {
        case throwable: Throwable => InternalServerError(ReturnPlainMessage("Internal server error.", false).jsonMessage)
      }
  }
}
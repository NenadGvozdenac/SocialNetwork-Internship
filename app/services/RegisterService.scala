package services

import dtos.ReturnLoginMessage
import exceptions.{UserAlreadyExistsExceptiuon, UserCouldNotBeCreatedException}
import models.Person
import repositories.UserRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegisterService @Inject() (repository: UserRepository) (implicit ex: ExecutionContext) {
  def getAllUsers : Future[Seq[Person]] = repository.getAllUsers

  def getUserById(id: Int) : Future[Option[Person]] = repository.getUserById(id)

  def getUserByUsername(username: String): Future[Option[Person]] = repository.getUserByUsername(username)

  def createUser(username: String, password: String) : Future[Option[Person]] = {
    for {
      _ <- if(username != "" && password != "") {
        Future.unit
      } else {
        Future.failed(new UserCouldNotBeCreatedException)
      }

      _ <- getUserByUsername(username).flatMap {
        case Some(_) => Future.failed(new UserAlreadyExistsExceptiuon)
        case None => Future.unit
      }

      newUser <- repository.createNewUser(new Person(username, password)).flatMap {
        case Some(value) => Future.successful(Some(value))
        case None => Future.failed(new UserCouldNotBeCreatedException)
      }
    } yield newUser
  }

  def validateUser(username: String, password: String) : Future[Option[Person]] = repository.validatePerson(username, password)

  def retrievePasswordByUsername(username: String): Future[Option[String]] = repository.retrievePasswordByUsername(username: String)

}

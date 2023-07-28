package repositories

import models.Person
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserRepository @Inject() (override protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  lazy val users = TableQuery[PersonTable]

  def getAllUsers: Future[Seq[Person]] = {
    db.run(users.result)
  }

  def getUserById(id: Int): Future[Option[Person]] = {
    db.run(users.filter(_.id === id).result).map(_.headOption)
  }

  def getUserByUsername(username: String) : Future[Option[Person]] = {
    db.run(users.filter(_.LoginName === username).result).map(_.headOption)
  }

  def validatePerson(username: String, password: String): Future[Option[Person]] = {
    db.run(users.filter(person => person.LoginName === username && person.LoginPassword === password).result).map(_.headOption)
  }

  def createNewUser(person: Person): Future[Option[Person]] = {
    val insertAction = users += person

    val retrieveAction = insertAction.flatMap { _ =>
      users.filter(_.LoginName === person.username).result.headOption
    }

    db.run(retrieveAction)
  }

  def retrievePasswordByUsername(username: String): Future[Option[String]] = {
    db.run(users.filter(user => user.LoginName === username).result).map(user => user.map(user => user.password).headOption)
  }

  class PersonTable(tag: Tag) extends Table[Person](tag, "users") {
    def id = column[Int]("PersonID", O.AutoInc, O.PrimaryKey)
    def LoginName = column[String]("LoginName")

    def LoginPassword= column[String]("LoginPassword")
    override def * = (id, LoginName, LoginPassword) <> (Person.tupled, Person.unapply)
  }
}
package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class PeopleFromDatabase(people: Seq[CreatePerson]) {
  val jsonMessage = Json.obj("people" ->
    people.map(person => Json.obj(
    "username" -> person.username,
    "password" -> person.password
  ))
  )
}

object PeopleFromDatabase {
  implicit val createPeopleFromDatabaseReads:   Reads[PeopleFromDatabase]   = Json.reads[PeopleFromDatabase]
  implicit val createPeopleFromDatabaseWrites:  Writes[PeopleFromDatabase]  = Json.writes[PeopleFromDatabase]
}
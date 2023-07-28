package dtos

import play.api.libs.json.{Json, Reads, Writes}

case class CreatePerson(username: String, password: String)

object CreatePerson {
  implicit val createPersonReads:   Reads[CreatePerson]   = Json.reads[CreatePerson]
  implicit val createPersonWrites:  Writes[CreatePerson]  = Json.writes[CreatePerson]
}
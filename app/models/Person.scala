package models

import play.api.libs.json.{Json, Reads, Writes}

case class Person(id: Int, username: String, password: String) {
  def this(username: String, password: String) = this(0, username, password)
}
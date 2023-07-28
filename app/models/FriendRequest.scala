package models

case class FriendRequest(requestID: Int, usernameFrom: String, usernameTo: String, requestStatus: String) {
  def this(usernameFrom: String, usernameTo: String, requestStatus: String) = {
    this(0, usernameFrom, usernameTo, requestStatus)
  }
}
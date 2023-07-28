package models

case class Post (id: Int, usernameOfPoster: String, numberOfLikes: Int, title: String, caption: String) {

  override def toString: String =
    id + ", " +
      ", USER: "  + usernameOfPoster +
      ", NOL: "   + numberOfLikes +
      ", T: "     + title +
      ", C: "     + caption

  def this(title: String, caption: String, username: String) = {
    this(0, username, 0, title, caption)
  }
}
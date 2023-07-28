package exceptions

class PostCouldNotBeCreatedException extends Exception

case class PostCouldNotBeEditedException (message: String) extends Exception

class PostCouldNotBeFoundException extends Exception
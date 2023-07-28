package models

import io.jsonwebtoken.{Jwts, SignatureAlgorithm}
import services.RegisterService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class JWTToken @Inject()(val registerService: RegisterService) (implicit ec: ExecutionContext) {

  private val secretKey = "s0meR@ndomSecr3tK3y#123"
  def createJwtToken(username: String): String = {

    val token = Jwts.builder
      .setSubject(username)
      .setHeaderParam("username", username)
      .signWith(SignatureAlgorithm.HS256, secretKey)
      .compact

    token
  }

  def validateJwtToken (token: String, password: String): Future[Boolean] = {
    try {
      val data = retrieveData(token)
      val username = data

      registerService.validateUser(username, password).map {
        case Some(value) => true
        case _ => false
      }
    } catch {
      case _ => Future.successful(false)
    }
  }

  def retrieveData(token: String) : String = {
    val claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)

    val headerParams = claims.getHeader
    val username: String = headerParams.get("username").asInstanceOf[String]

    username
  }
}
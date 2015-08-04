package controllers

import jp.t2v.lab.play2.auth.LoginLogout
import models.Account
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import services.AuthConfigImpl

import scala.concurrent.Future

object Sessions extends Controller with LoginLogout with AuthConfigImpl {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])

  import models.Account._

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded.map(_.flashing(
      "success" -> "You've been logged out"
    ).removingFromSession("rememberme"))

  }

  def authenticate = Action.async(parse.json) {
    implicit request => request.body.validate[Account].map {
      profil: Account => {

        Account.authenticate(profil.email, profil.password) match {
          case aCC => {
            //TODO always set cookie to be refactored
            val req = request.copy(tags = request.tags + ("rememberme" -> "true"))
            gotoLoginSucceeded(aCC.get._id)(req, defaultContext).map(_.withSession("rememberme" -> "true"))
          }
          case _ => Future.successful(BadRequest(Json.obj("result" -> "could not get user from db")))
        }
      }
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }
}

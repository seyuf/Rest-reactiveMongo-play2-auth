package services

import jp.t2v.lab.play2.auth._
import models.{Role, Account}
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
 * Created by madalien on 26/04/15.
 */
trait AuthConfigImpl extends AuthConfig{
  type Id = Option[BSONObjectID]
  type Authority = Role
  type User = Account
 // val idTag:ClassTag[Id] =  classTag[Id]
  val idTag: ClassTag[Id] = classManifest[Id]


  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = {
    var userId: String = "toto"
    id match {
      case iD:BSONObjectID =>  userId = iD.toString()
      case Some(acc:BSONObjectID) =>  userId =  acc.stringify
    }
    Future.successful(Account.findById(userId))
  }

  def sessionTimeoutInSeconds: Int = 2592000

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Results.Ok(Json.obj("result" -> "your're connected")))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = {
    Future.successful(Results.Ok(Json.obj("result" -> "your're logged out")))
  }

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = {
    Future.successful(Results.Forbidden(Json.obj("result" -> "authentification failed")))
  }

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit ctx: ExecutionContext): Future[Result] = {
    Future.successful(Results.Unauthorized(Json.obj("result" -> "authorization failed")))
  }

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = throw new AssertionError

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    var rights:String = ""
    user.role match {
      case Some(right) => rights = right
      case _ => rights = ""
    }

    /*
    (user.role, authority) match {
     case (Some("Administrator"), _) => true
     case (Some("NormalUser"), NormalUser) => true
     case _ => false
    }
    */
    val currentRole = Role.stringValueOf(authority)
    println(s" current role is ======>", currentRole, "user rights" , rights)
    (rights, currentRole) match {
      case ("NormalUser", "NormalUser") => true
      case ("Administrator", _)       => true
      case _  => false
    }
  }

  override lazy val tokenAccessor = new CustomCookieAccessor(sessionTimeoutInSeconds)


  /*
override lazy val tokenAccessor = new CookieTokenAccessor(
   * Whether use the secure option or not use it in the cookie.
   * However default is false, I strongly recommend using true in a production.
  cookieSecureOption = play.api.Play.isProd(play.api.Play.current),
 //  cookieSecureOption = play.api.Play.isDev(play.api.Play.current),
    cookieMaxAge       = Some(sessionTimeoutInSeconds)
)


   */


}

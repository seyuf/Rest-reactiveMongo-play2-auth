package models

/**
 * Created by madalien on 26/04/15.
 */

import java.util.concurrent.TimeUnit

import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.commands.LastError

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Created by madalien on 05/04/15.
 */

case class Account(
                  _id: Option[BSONObjectID] = None,
                  role: Option[String] = None,
                  email: String,
                  firstName: Option[String] = None,
                  lastName: Option[String] = None,
                  password: String,
                  avatar_path: Option[String] = None,
                  update_date: Option[DateTime] = Some(new DateTime())
                  )

object Account extends Controller with MongoController {

  import play.modules.reactivemongo.json.BSONFormats._
  import play.api.libs.json.Json


  implicit val AccountFormat = Json.format[Account] ;
  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])
  def collection: JSONCollection = db.collection[JSONCollection]("Accounts")



  def authenticate(email: String, password: String): Option[Account] = {
    val cursor  = collection.find(
      Json.obj("email"->email, "password"-> password)
    ).cursor[Account]
    Await.result(cursor.headOption, Duration(5, TimeUnit.SECONDS))
  }

  def findByEmail(email: String): Option[Account] =  {
    val cursor  = collection.find(
        Json.obj("email"->email)
    ).cursor[Account]
    Await.result(cursor.headOption, Duration(5, TimeUnit.SECONDS))
  }

  def findById(id: String): Option[Account] = {
   val cursor  = collection.find(
        Json.obj("_id" -> BSONObjectID(id))
   ).cursor[Account]
    Await.result(cursor.headOption, Duration(5, TimeUnit.SECONDS))
  }

  def findAll(): Seq[Account] = {
     val cursor  = collection.find(
        Json.obj()
   ).sort(Json.obj("update_date" -> -1))
       .cursor[Account].collect[List]()
    Await.result(cursor, Duration(5, TimeUnit.SECONDS))
  }

  def create(account: Account):Future[LastError] = {
     val updatedUser = account.copy(update_date = Some(new DateTime()));
        val selec = BSONDocument("email" -> account.email);
        collection.update(selec, updatedUser, upsert = true)
  }
}

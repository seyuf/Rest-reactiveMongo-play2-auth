package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.Role.{Administrator, NormalUser}
import org.slf4j.{Logger, LoggerFactory}
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, _}
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.core.protocol.QueryFlags
import services.AuthConfigImpl
import models.Account
import play.api.Play
import play.api.libs.json.Json
import play.api.mvc.{Controller}
import models.Account
import reactivemongo.core.protocol.QueryFlags
import reactivemongo.api.{QueryOpts}
import jp.t2v.lab.play2.auth.AuthElement
import models.Role.{NormalUser, Administrator}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument
import services.AuthConfigImpl
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{ MongoController}
import org.slf4j.{LoggerFactory, Logger}
import reactivemongo.api.Cursor
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

/**
 * Created by madalien on 27/04/15.
 */
object AccountsCtrl extends Controller with MongoController  with AuthElement with AuthConfigImpl {
  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])

  def collection: JSONCollection = db.collection[JSONCollection]("Accounts")

  import models.Account._

  def adminCheck = StackAction(AuthorityKey -> Administrator){
    implicit request => {
      loggedIn match {
        case user:Account => Ok(Json.obj("user" -> user))
        case _ => Ok(Json.obj("user" -> "there is no current user found"))
      }
    }
  }


  //TODO remove password from the user
  def currentUser = AsyncStack(AuthorityKey -> Administrator, AuthorityKey -> NormalUser){
    implicit request => {
      loggedIn match {
        case user:Account => {
          val updatedUser = user.copy(password = "Well Tried friend");
          Future.successful(Ok(Json.obj("user" -> updatedUser)))
        }
        case _ => Future.successful(Ok(Json.obj("result" -> "there is no current user found")))
      }
    }
  }

  //TODO Admin with the rights to modify the email
  def modifyUser = AsyncStack(parse.json, AuthorityKey -> Administrator, AuthorityKey -> NormalUser) {
    implicit request => request.body.validate[Account].map {
      profile => {
        loggedIn match {
          case user if(user.role.get == "Administrator") => {
            val profileTmp = profile.copy(_id = None)
            val selec = BSONDocument("_id" -> profile._id.get)
            collection.update(selec, profileTmp, upsert = false).map{
              lastError => lastError.inError match {
                case true => {
                  logger.debug(s"error at atelier update : $lastError")
                  InternalServerError(s"error at user's update : $lastError")
                }
                case false =>{
                  Ok(s"user have been updated: $lastError")
                }
              }
            }
          }

          case user if (user._id.get == profile._id.get) => {
            val profileTmp = profile.copy(_id = None)
            Account.create(profileTmp).map {
              lastError => logger.debug(s"user successfully modified with LastError: $lastError")
                Ok(s"User have been successfully modified")
            }
          }


          case x =>{
            Future.successful(Ok(s"sorry you are not allowed to modify this entity "+x.role))
          }
        }
      }

    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def modifyUserBasics = AsyncStack(parse.json, AuthorityKey -> Administrator, AuthorityKey -> NormalUser) {
    implicit request => request.body.validate[Account].map {
      profile => {
        val newPassword = (request.body.as[JsObject]\ "newPassword").asOpt[String].getOrElse("")
        loggedIn match {
          case user if(newPassword != "") =>{

            if(user.password != profile.password) {
              Future.successful(BadRequest(s"L'ancien mot de passe est erronée."))
            }
            else {
              if (user._id.get == profile._id.get) {
                val profileTmp = profile.copy(_id = None, password = newPassword)
                Account.create(profileTmp).map {
                  lastError => logger.debug(s"user successfully modified with LastError: $lastError")
                    Ok(s"User have been successfully modified")
                }
              }
              else {
                Future.successful(BadRequest(s"Vous n'êtes pas autorisé à modifier cet utilisateur."))
              }
            }
          }
          case user if (user._id.get == profile._id.get) => {
            val profileTmp = profile.copy(_id = None)
            Account.create(profileTmp).map {
              lastError => logger.debug(s"user successfully modified with LastError: $lastError")
                Ok(s"User have been successfully modified")
            }
          }

          case x =>{
            Future.successful(Ok(s"sorry you are not allowed to modify this entity "+x.role))
          }
        }
      }

    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  //TODO implement search user by regex with search form
  // page must start at 1
  def getUsers(page: Int, numberByPage: Int) =  AsyncStack(AuthorityKey -> Administrator) {

    implicit request => {
      val cursor: Cursor[Account] = collection.
        find(Json.obj()).options(QueryOpts((page - 1)* numberByPage, numberByPage, QueryFlags.Exhaust)).
        //sort(Json.obj("created" -> -1)).
        cursor[Account]
      val futureUserList: Future[List[Account]] = cursor.collect[List]()
      val futureUsersJsonArray: Future[JsArray] = futureUserList.map { users =>
        Json.arr(users)
      }
      futureUsersJsonArray.map {
        users =>
          Ok(users(0))
      }
    }
  }


  def byAtelier(id: String) =  AsyncStack(AuthorityKey -> Administrator, AuthorityKey -> NormalUser) {
    implicit request => {
      val cursor: Cursor[Account] = collection.
        find(Json.obj("ateliers" -> id)).
        cursor[Account]
      val futureUserList: Future[Array[Account]] = cursor.collect[Array]()
      val futureUsersJsonArray: Future[JsArray] = futureUserList.map { users =>


        if(loggedIn.role.getOrElse("NormalUser") != "Administrator") {
          for (i <- 0 until users.length) {
            users(i) = users(i).copy(password = "Well Tried friend");
          }
        }
        Json.arr(users)
      }
      futureUsersJsonArray.map {
        users => Ok(users(0))
      }
    }
  }

  def byId(id: String) =  AsyncStack(AuthorityKey -> Administrator, AuthorityKey -> NormalUser) {
    implicit request => {
      val cursor: Cursor[Account] = collection.
        find(Json.obj("_id" -> BSONObjectID(id))).
        cursor[Account]
      val futureUserList: Future[Array[Account]] = cursor.collect[Array]()
      val futureUsersJsonArray: Future[JsArray] = futureUserList.map { users =>
         if(loggedIn.role.getOrElse("NormalUser") != "Administrator") {
          for (i <- 0 until users.length) {
            users(i) = users(i).copy(password = "Well Tried friend");
          }
        }
        Json.arr(users)
      }
      futureUsersJsonArray.map {
        users =>
          Ok(users(0))
      }
    }
  }

  def createUser =  AsyncStack(parse.json, AuthorityKey -> Administrator){
    implicit request => request.body.validate[Account].map {
      profile: Account => {
        Account.create(profile).map{
          lastError => logger.debug(s"successfully inserted with LastError: $lastError")
            Created(s"User have been successfully created")
        }
      }
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def uploadAvatar(user_id: String) = AsyncStack(parse.multipartFormData, AuthorityKey -> Administrator, AuthorityKey -> NormalUser) {
    implicit request => request.body.file("file").map { file =>
      loggedIn match {

        case user if (user._id.get.stringify == user_id) => {
          import java.io.File
          val filename = file.filename.replace(' ', '_')
          val filePath = Play.current.path.getPath + "/avatar/" + user_id + "/" + filename;
          val fileUrl = "logos/avatar/" + user_id + "/" + filename;
          val contentType = file.contentType
          //val title = request.body.asFormUrlEncoded.get("title").get(0);
          val dir = new File(Play.current.path.getPath + "/avatar/" + user_id)
          dir.mkdirs()
          file.ref.moveTo(new File(filePath))
          val updatedUser = BSONDocument(
            "$set" -> BSONDocument(
              "avatar_path" -> fileUrl
            ))

          val selec = BSONDocument("_id" -> BSONObjectID(user_id));
          collection.update(selec, updatedUser, upsert = true).map {
            lastError => logger.debug(s"successfully inserted with LastError: $lastError")
              Created(s"user avatar successfully updated")
          }
        }
        case _ => {
          Future.successful(InternalServerError(Json.obj("result" -> s"your not authorized to modify this entity")))
        }
      }
    }.getOrElse {
      Future.successful(BadRequest(Json.obj("result" -> "user error: missing file or existing file")))
    }
  }

  def removeByIds(ids: List[String]) = AsyncStack(AuthorityKey -> Administrator) {
    implicit request => {
      val bsonIds = ids.map(id => Json.obj("$oid" -> id))
      val query = Json.obj("_id" -> Json.obj("$in" -> bsonIds))
      collection.
        remove(query).map {
        lastError => lastError.inError match {
          case true => {
            logger.debug(s"error while removing user(s) : $lastError")
            InternalServerError(s"error at while removing user(s) : $lastError")
          }
          case false => {
            Ok(s"User(s) Removal Success: $lastError")
          }
        }
      }
    }
  }

}


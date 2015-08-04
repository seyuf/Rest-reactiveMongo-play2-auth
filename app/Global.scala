/**
 * Created by madalien on 04/08/15.
 */
import models.Account
import org.joda.time.DateTime
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.{ Logger, Application, GlobalSettings }
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.bson.BSONDocument

object Global extends play.api.GlobalSettings{

  def db = ReactiveMongoPlugin.db
  def collection = db.collection[JSONCollection]("Accounts")


  val admin = new Account(_id = None, firstName = Some("Admin"), role=Some("Administrator"), lastName = Some("admin"),
    password = "admin", email = "admin@admin.com", update_date = Some(new DateTime()))

  override def onStart(app: Application) {
    Logger.info("Application has started")

    val selec = Json.obj("email" -> "admin@admin.com");
    collection.update(selec, admin, upsert = true).foreach(lastError => Logger.info(s"set admin in the db: $lastError"))
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")

    collection.drop().onComplete {
      case _ => Logger.info("Database collection dropped")
    }
  }
}
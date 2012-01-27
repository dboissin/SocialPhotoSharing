package dao

import play.api._

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.MongoDB
import com.novus.salat._
import com.mongodb.casbah.Imports._

case class MongoDBHelper(dbName: String, host: String = "localhost", port: Int = 27017) {
  lazy val connection: MongoConnection = MongoConnection(host, port)

  def db: MongoDB = connection(dbName)
  def collection(name:String): MongoCollection = db(name)
}

object Mongo {
  lazy val helper: MongoDBHelper = {
    Logger.info("init helper")
    val app = Play.current
    var result = MongoDBHelper(app.configuration.getString("mongodb.db").get)
    app.configuration.getString("mongodb.host") match {
      case Some(host) => { result = result.copy(host = host) }
      case _ =>
    }
    app.configuration.getString("mongodb.port") match {
      case Some(port) => { result = result.copy(port = Integer.parseInt(port)) }
      case _ =>
    }
    result
  }

  import com.mongodb.casbah.gridfs._

  lazy val gridfs = GridFS(helper.db)

  def connection(implicit app: Application):MongoConnection = helper.connection

  def db(implicit app: Application):MongoDB = helper.db

  def collection(name: String)(implicit app :Application):MongoCollection = helper.db(name)

  def salatCollection[T <: Product](name: String)(implicit app: Application):
      MongoCollectionProxy[T] = new MongoCollectionProxy[T](collection(name))

}


object `package` {
  type ObjectId = org.bson.types.ObjectId

  implicit def currentApplication: Application = Play.maybeApplication.get
  implicit def mongoCollectionProxyToMongoCollection[T<:Product]
      (proxy :MongoCollectionProxy[T]): MongoCollection = proxy.collection
  implicit def stringToObjectId(id :String): ObjectId = new ObjectId(id)

  import play.api.libs.json._

  implicit object ObjectIdFormat extends Reads[ObjectId] {
    def reads(json: JsValue): ObjectId = json match {
        case JsString(s) if (s != null && org.bson.types.ObjectId.isValid(s))  => new ObjectId(s)
        case _ => new ObjectId
    }
  }

  implicit object OptionObjectIdFormat extends Reads[Option[ObjectId]] {
    def reads(json: JsValue): Option[ObjectId] = json match {
        case JsString(s) if (s != null && org.bson.types.ObjectId.isValid(s))  => Some(new ObjectId(s))
        case _ => None
    }
  }
}

class MongoCollectionProxy[T<:Product](val collection :MongoCollection) {
  import com.novus.salat._
  import com.novus.salat.annotations._
  import com.novus.salat.global._
  import com.mongodb.casbah.commons.Imports._
  import com.novus.salat.dao._

  def save(product: T)(implicit m: Manifest[T]) =
    collection.save(grater[T].asDBObject(product))

  def dao[K<:Any](implicit app :Application, t :Manifest[T], k:Manifest[K]) =
    new SalatDAO[T, K](collection) {}
}

package object SalatContext {

  implicit val ctx = new Context {
      val name = "Salat-Context"
  }

  ctx.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")

}

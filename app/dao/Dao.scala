package dao

import model._
import Constants.Constants._
import dao.SalatContext._
import com.mongodb.casbah.Imports._
import com.novus.salat.dao._
import play.Logger
import play.api.Play

object PhotoDAO extends SalatDAO[Photo, ObjectId](collection = Mongo.collection(PHOTOS_COLLECTION))

object Crud {

  def add[T,K](o:T, f: (T) => Option[K]) = {
    try {
      val oid = f(o)
      if (!oid.isDefined) {
        Logger.error("Error during add.")
      }
      oid
    } catch {
      case e: SalatInsertError =>
        Logger.error("Error during add : %s".format(e.getCause))
        None
    }
  }

  def update[T,K](id: K, o:T)(f: (DBObject, T, Boolean, Boolean, WriteConcern) => Unit) = {
    try {
      f(MongoDBObject("_id" -> id), o, false, false, new WriteConcern)
      Some(id)
    } catch {
      case e: SalatDAOUpdateError =>
        Logger.error("Error during update : %s".format(e.getCause))
        None
    }
  }

}

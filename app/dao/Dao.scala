package dao

import model._

import Constants.Constants._
import dao.SalatContext._
import com.mongodb.casbah.Imports._
import com.novus.salat.dao._
import play.Logger
import play.api.Play
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Random
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import play.api.libs.Codecs._
import play.api.libs.ws.SignatureCalculator
import play.api.libs.concurrent._

trait MongoPhotoRepositoryComponent extends PhotoRepositoryComponent {
  def photoRepository = new MongoPhotoRepository

  class MongoPhotoRepository extends
      SalatDAO[Photo, String](collection = Mongo.collection(PHOTOS_COLLECTION))
      with PhotoRepository {

    lazy val df = new SimpleDateFormat("yyyyMMddHHmmss")
    lazy val random = new Random

    def add(photo: Photo, f: FilePart[TemporaryFile], calc: Option[SignatureCalculator]) = {
      Promise.pure(addFile(f) match {
        case Some(oid) => Crud.add(photo.copy(ref = oid.toString), insert)
        case _ => None
      })
    }

    def addComment(photoId: String, comment: String,
        calc: Option[SignatureCalculator]): Promise[Option[String]] = {
      Promise.pure(None)
    }

    private def addFile(file: FilePart[TemporaryFile],
        filename:Option[String] = None): Option[ObjectId] = {
      val gfif = Mongo.gridfs.createFile(file.ref.file)
      gfif.filename = filename.getOrElse("%s_%s".format(df.format(new Date), sha1(random.nextString(10))))
      gfif.contentType = file.contentType.getOrElse("")
      gfif.save
      gfif._id
  }
  }
}


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

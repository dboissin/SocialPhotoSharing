package model

import dao._
import play.api.libs.json._
import play.api.libs.Files._
import play.api.mvc.MultipartFormData.FilePart
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Random
import play.api.libs.Codecs._

case class Photo(
  id: ObjectId,
  name: String,
  description: String,
  originalPhoto: Option[ObjectId],
  viewablePhoto: Option[ObjectId],
  comments : List[Comment]
)

case class Comment(
  id: ObjectId,
  comment: String
)


object Photo {

  lazy val df = new SimpleDateFormat("yyyyMMddHHmmss")
  lazy val random = new Random

  def apply(p: Map[String, Seq[String]]): Photo = Photo(
    p.getOrElse("id", List((new ObjectId).toString)).head,
    p.getOrElse("name", List("")).head,
    p.getOrElse("description", List("")).head,
    None, None, Nil
  )

  def add(p: Photo, vp: FilePart[TemporaryFile], op: Option[FilePart[TemporaryFile]]) = {
    addFile(vp) match {
      case Some(vpId) if (op.isDefined) =>
        val opId = addFile(op.get)
        Crud.add(p.copy(viewablePhoto = Some(vpId), originalPhoto = opId), PhotoDAO.insert)
      case Some(vpId) => Crud.add(p.copy(viewablePhoto = Some(vpId)), PhotoDAO.insert)
      case _ => None
    }
  }

  private def addFile(file: FilePart[TemporaryFile], filename:Option[String] = None):Option[ObjectId] = {
      val gfif = Mongo.gridfs.createFile(file.ref.file)
      gfif.filename = filename.getOrElse("%s_%s".format(df.format(new Date), sha1(random.nextString(10))))
      gfif.contentType = file.contentType.getOrElse("")
      gfif.save
      gfif._id
  }
}

package model

import dao._
import play.api.libs.Files._
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.ws.SignatureCalculator
import play.api.libs.concurrent.Promise
import scala.collection.immutable.StringOps

object SafetyLevel extends Enumeration {
  type SafetyLevel = Value
  val SAFE = Value(1)
  val MODERATE = Value(2)
  val RESTRICTED = Value(3)
}

import SafetyLevel._

case class Photo(
  id: String,
  title: String,
  description: String,
  ref: String,
  comments : List[Comment],
  tags: List[Tag] = Nil,
  safety: Int = RESTRICTED.id,
  groups: List[String] = Nil // ids des groupes qui peuvent afficher la photo
)

case class Comment(comment: String)
case class Tag(tag: String)
case class Group(id: String)


trait PhotoServiceComponent {
  def photoService: PhotoService

  trait PhotoService {
    def add(p: Map[String, Seq[String]], vp: FilePart[TemporaryFile],
        calc: Option[SignatureCalculator]): Promise[Option[String]]

    def addComment(photoId: String, comment: String,
        calc: Option[SignatureCalculator]): Promise[Option[String]]
  }
}

trait DefaultPhotoServiceComponent extends PhotoServiceComponent {
  this: PhotoRepositoryComponent =>
  def photoService = new DefaultPhotoService

  class DefaultPhotoService extends PhotoService {
    def add(p: Map[String, Seq[String]], vp: FilePart[TemporaryFile],
      calc: Option[SignatureCalculator]) = {
      val photo = Photo(
      p.getOrElse("id", List((new ObjectId).toString)).head,
      p.getOrElse("name", List("")).head,
      p.getOrElse("description", List("")).head,
      "", Nil,
      p.getOrElse("tags", List("")).head.split(',').map(t => Tag(t.trim)).toList,
      new StringOps(p.getOrElse("safety", List(RESTRICTED.id.toString)).head).toInt,
      p.getOrElse("groups", List("public")).head.split(',').map(_.trim).toList // TODO check if group exists
      )
      photoRepository.add(photo, vp, calc)
    }

    def addComment(photoId: String, comment: String,
        calc: Option[SignatureCalculator]) = {
      photoRepository.addComment(photoId, comment, calc)
    }
  }
}


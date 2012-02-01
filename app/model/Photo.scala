package model

import dao._
import play.api.libs.Files._
import play.api.mvc.MultipartFormData.FilePart


case class Photo(
  id: String,
  title: String,
  description: String,
  ref: String,
  comments : List[Comment]
)

case class Comment(comment: String)

trait PhotoServiceComponent {
  def photoService: PhotoService

  trait PhotoService {
    def add(p: Map[String, Seq[String]], vp: FilePart[TemporaryFile]): Option[String]
  }
}

trait DefaultPhotoServiceComponent extends PhotoServiceComponent {
  this: PhotoRepositoryComponent =>
  def photoService = new DefaultPhotoService

  class DefaultPhotoService extends PhotoService {
    def add(p: Map[String, Seq[String]], vp: FilePart[TemporaryFile]) = {
      val photo = Photo(
      p.getOrElse("id", List((new ObjectId).toString)).head,
      p.getOrElse("name", List("")).head,
      p.getOrElse("description", List("")).head,
      "", Nil
      )
      photoRepository.add(photo, vp)
    }
  }
}


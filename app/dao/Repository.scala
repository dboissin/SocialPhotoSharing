package dao

import model._
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile

trait PhotoRepositoryComponent {
  def photoRepository : PhotoRepository

  trait PhotoRepository {
    def add(photo: Photo, f: FilePart[TemporaryFile]): Option[String]
  }

}

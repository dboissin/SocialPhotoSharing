package dao

import model._
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import play.api.libs.ws.SignatureCalculator
import play.api.libs.concurrent.Promise

trait PhotoRepositoryComponent {
  def photoRepository : PhotoRepository

  trait PhotoRepository {
    def add(photo: Photo, f: FilePart[TemporaryFile],
        calc: Option[SignatureCalculator]): Promise[Option[String]]

    def addComment(photoId: String, comment: String,
        calc: Option[SignatureCalculator]): Promise[Option[String]]

  }

}

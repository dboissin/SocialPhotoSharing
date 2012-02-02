package ws

import dao.PhotoRepositoryComponent
import model.Photo
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile

trait FlickPhotoRepositoryComponent extends PhotoRepositoryComponent {
  def photoRepository = new FlickrPhotoRepository

  class FlickrPhotoRepository extends PhotoRepository {

    def add(photo: Photo, f: FilePart[TemporaryFile]) = {
      None
    }

  }

}

package ws

import dao.PhotoRepositoryComponent
import model.Photo
import controllers.Flickr
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import play.api.libs.ws._
import play.api.libs.wsutil._
import com.ning.http.client.{FilePart => AHCFilePart}
import com.ning.http.client.StringPart
import play.api.libs.oauth._
import play.api.Logger

trait FlickrPhotoRepositoryComponent extends PhotoRepositoryComponent {
  def photoRepository = new FlickrPhotoRepository

  class FlickrPhotoRepository extends PhotoRepository {

    val host = "http://api.flickr.com/services"

    def add(photo: Photo, f: FilePart[TemporaryFile], calc: Option[SignatureCalculator]) = {
      val url = host + "/upload"
      WSUtil.post(url, List(
          new AHCFilePart("photo", f.ref.file, "application/octet-stream", ""),
          new StringPart("title", photo.title),
          new StringPart("description", photo.description)
      ), calc).map{res =>
        Logger.debug("%s - %s".format(res.status, res.body))
        if (res.status == 200) (res.json \ "photoid").asOpt[String] else None
      }
    }

  }

}

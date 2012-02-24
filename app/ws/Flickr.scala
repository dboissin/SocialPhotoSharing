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
import java.net.URLEncoder._

trait FlickrPhotoRepositoryComponent extends PhotoRepositoryComponent {
  def photoRepository = new FlickrPhotoRepository

  class FlickrPhotoRepository extends PhotoRepository {

    val host = "http://api.flickr.com/services"

    def add(photo: Photo, f: FilePart[TemporaryFile], calc: Option[SignatureCalculator]) = {
      val url = host + "/upload"
      val qs = "/?title=%s&description=%s"
        .format(encode(photo.title, "UTF-8"), encode(photo.description, "UTF-8"))
      WSUtil.post(url + qs, List(
          new AHCFilePart("photo", f.ref.file, "application/octet-stream", ""),
          new StringPart("title", photo.title),
          new StringPart("description", photo.description)
      ), calc).map{res =>
        Logger.debug("%s - %s".format(res.status, res.body))
        if (res.status == 200) Some((res.xml \\ "photoid").text) else None
      }
    }

    def addComment(photoId: String, comment: String,
        calc: Option[SignatureCalculator]) = {
      val url = host + "/rest"
      val body = """method=flickr.photos.comments.addComment&format=json&nojsoncallback=1&photo_id=%s&comment_text=%s"""
        .format(encode(photoId, "UTF-8"), encode(comment, "UTF-8"))
      Logger.debug("Comment request : %s".format(body))
      Logger.debug("calc : %s".format(calc))
        WS.url(url + "/?" + body).sign(calc.get).post("").map{ res =>
        Logger.debug("%s - %s".format(res.status, res.body))
        if (res.status == 200) Some(res.body) else None
      }
    }

  }

}

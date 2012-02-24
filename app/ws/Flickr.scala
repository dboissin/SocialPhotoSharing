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
import com.ning.http.client.Part
import play.api.libs.oauth._
import play.api.Logger
import java.net.URLEncoder._
import scala.collection.mutable.HashMap

trait FlickrPhotoRepositoryComponent extends PhotoRepositoryComponent {
  def photoRepository = new FlickrPhotoRepository

  class FlickrPhotoRepository extends PhotoRepository {

    val host = "http://api.flickr.com/services"

    def add(photo: Photo, f: FilePart[TemporaryFile], calc: Option[SignatureCalculator]) = {
      val url = host + "/upload"
      val optionnals = prepareOptionnalAdd(photo)
      val body = if (!optionnals._1.isEmpty) {
        ("/?" + optionnals._1.substring(1), optionnals._2)
      } else ("", Nil)
      Logger.debug("body : %s".format(body))
      WSUtil.post(url + body._1,
          new AHCFilePart("photo", f.ref.file, "application/octet-stream", "") :: body._2
       , calc).map{res =>
        Logger.debug("%s - %s".format(res.status, res.body))
        if (res.status == 200) Some((res.xml \\ "photoid").text) else None
      }
    }

    def prepareOptionnalAdd(photo: Photo) = {
      val optionnal = new HashMap[String, Part]()
      if (photo.title != null && !photo.title.trim.isEmpty)
        optionnal.put("title=%s".format(encode(photo.title, "UTF-8")),
            new StringPart("title", photo.title))
      if (photo.description != null && !photo.description.trim.isEmpty)
        optionnal.put("description=%s".format(encode(photo.description, "UTF-8")),
            new StringPart("description", photo.description))
      if (photo.tags != null && photo.tags.size > 0) {
        val tags = photo.tags.map(_.tag).mkString(" ")
        optionnal.put("tags=%s".format(encode(tags, "UTF-8")),
            new StringPart("tags", tags))}
      if (photo.safety > 1)
        optionnal.put("safety_level=%s".format(encode(photo.safety.toString, "UTF-8")),
            new StringPart("safety_level", photo.safety.toString))
      if (photo.groups != null) {
        photo.groups.filter(g => g == "friend" || g == "public" || g == "family").foreach(_ match {
          case "friend" =>
            optionnal.put("is_friend=%s".format(encode(1.toString, "UTF-8")),
            new StringPart("is_friend", 1.toString))
          case "public" =>
            optionnal.put("is_public=%s".format(encode(1.toString, "UTF-8")),
            new StringPart("is_public", 1.toString))
          case "family" =>
            optionnal.put("is_family=%s".format(encode(1.toString, "UTF-8")),
            new StringPart("is_family", 1.toString))
        })}
      optionnal./:[(String, List[Part])]("", Nil)((acc, c) => (acc._1 + "&" + c._1, c._2 :: acc._2))
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

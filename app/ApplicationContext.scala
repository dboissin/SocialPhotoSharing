import play.api._
import model.DefaultPhotoServiceComponent
import dao.MongoPhotoRepositoryComponent
import ws.FlickrPhotoRepositoryComponent

package Context {

object ApplicationContext {

  val photoServiceComponent = Play.current.configuration.getString("storage.repository") match {
    case Some("flickr") =>
      Logger.info("DefaultPhotoServiceComponent with FlickrPhotoRepositoryComponent")
      new DefaultPhotoServiceComponent with FlickrPhotoRepositoryComponent
    case _ =>
      Logger.info("DefaultPhotoServiceComponent with MongoPhotoRepositoryComponent")
      new DefaultPhotoServiceComponent with MongoPhotoRepositoryComponent
  }
  val photoService = photoServiceComponent.photoService
}

}

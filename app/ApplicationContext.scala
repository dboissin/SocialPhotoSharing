
import model.DefaultPhotoServiceComponent
import dao.MongoPhotoRepositoryComponent

package Context {

object ApplicationContext {

  val photoServiceComponent = new DefaultPhotoServiceComponent with
      MongoPhotoRepositoryComponent
  val photoService = photoServiceComponent.photoService
}

}

package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import Json._
import model._
import dao.ObjectId
import JsonCommon._
import play.api.libs.concurrent.Promise

object Photos extends Controller {
  import Context.ApplicationContext.photoService

  def uploadPhoto = Action{ request =>
    Async {
      (request.body.asMultipartFormData match {
        case Some(form) if (form.file("viewable_photo").isDefined) =>
          photoService.add(form.asFormUrlEncoded,
            form.file("viewable_photo").get, Flickr.oAuthCalculator(request))
        case _ => Promise.pure(None)
      }).map( _ match {
        case Some(id) => Ok(id)
        case _ => BadRequest("Photo uploading error!")
      })
    }
  }

}

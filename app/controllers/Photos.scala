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

  def comment = Action { request =>
    Async {
    val json = request.body.asJson.getOrElse(JsNull)
    val photoId = (json \ "photo_id").as[String]
    val comment = (json \ "comment").as[String]
    photoService.addComment(photoId, comment, Flickr.oAuthCalculator(request))
    .map( _ match {
        case Some(res) => Ok(res)
        case _ => BadRequest("Add comment error !")
      })
  }}

  def testComment = Action { request =>
    Async {
    photoService.addComment("6701065553", "testcommentonapi", Flickr.oAuthCalculator(request))
    .map(res => Ok(res.getOrElse("none")))
    }
  }
}

package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import Json._
import model._
import dao.ObjectId
import JsonCommon._

object Photos extends Controller {
  import Context.ApplicationContext.photoService

  private def res(oid: Option[String]) = oid match {
    case Some(id) => Ok(JsObject(List("id" -> JsString(id))).toString)
    case _ => BadRequest("Processing error.")
  }

  def uploadPhoto = Action{ request =>
    res(request.body.asMultipartFormData match {
      case Some(form) if (form.file("viewable_photo").isDefined) =>
        photoService.add(form.asFormUrlEncoded,
            form.file("viewable_photo").get)
      case _ => None
    })
  }

}

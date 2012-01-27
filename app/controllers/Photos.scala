package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import Json._
import model._
import dao.ObjectId
import JsonCommon._

object Photos extends Controller {

  private def res(oid: Option[ObjectId]) = oid match {
    case Some(id) => Ok(toJson(id))
    case _ => BadRequest("Processing error.")
  }

  def uploadPhoto = Action{ request =>
    res(request.body.asMultipartFormData match {
      case Some(form) if (form.file("viewable_photo").isDefined) =>
        Photo.add(Photo(form.asFormUrlEncoded),
            form.file("viewable_photo").get, form.file("original_photo"))
      case _ => None
    })
  }

}

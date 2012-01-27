package controllers

import dao.ObjectId
import play.api.libs.json._

object JsonCommon {

  implicit object ResponseObjectIdFormat extends Writes[ObjectId] {
    def writes(oid: ObjectId): JsValue = JsObject(List(
      "id" -> JsString(oid.toString)))
  }

}

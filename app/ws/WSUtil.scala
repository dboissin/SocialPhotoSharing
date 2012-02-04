package play.api.libs.wsutil {

import com.ning.http.client.Part
import play.api.libs.ws._
import WS._
import play.api.libs.iteratee.Iteratee
import play.api.libs.concurrent.Promise

  object WSUtil {

    def post(url: String, bodyParts: List[Part],
        calc: Option[SignatureCalculator] = None) = prepare("POST", url, bodyParts, calc).execute

    def postAndRetrieveStream[A](url: String, bodyParts: List[Part],
        calc: Option[SignatureCalculator] = None)
        (consumer: ResponseHeaders => Iteratee[Array[Byte], A]): Promise[Iteratee[Array[Byte], A]] =
        prepare("POST", url, bodyParts, calc).executeStream(consumer)

    private def prepare(method: String, url: String, bodyParts: List[Part],
        calc: Option[SignatureCalculator]) = {
       val wsr = new WSRequest("POST", None, calc).setUrl(url)
        .setHeaders(Map("Content-Type" -> Seq("multipart/form-data")))
      bodyParts.foreach(wsr.addBodyPart(_))
      wsr
    }
  }
}

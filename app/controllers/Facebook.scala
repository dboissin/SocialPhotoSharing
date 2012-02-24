package controllers

import play.api._
import play.api.mvc._
import java.net.URLEncoder
import play.api.libs.ws._

object Facebook extends Controller {

  val conf = Play.current.configuration
  val clientId = conf.getString("facebook.key").getOrElse("")
  val clientSecret = conf.getString("facebook.secret").getOrElse("")
  val redirectUri = URLEncoder.encode("http://localhost:9000/facebook/auth", "UTF-8")

  def authenticate = Action { request =>
    request.queryString.get("error_reason").flatMap(_.headOption).map(
        BadRequest(_))
    .getOrElse{
      request.queryString.get("code").flatMap(_.headOption).map{ code =>
        val accessTokenUrl = "https://graph.facebook.com/oauth/access_token?" +
          "client_id=%s&redirect_uri=%s&client_secret=%s&code=%s"
          .format(clientId, redirectUri, clientSecret, code)
        val res = WS.url(accessTokenUrl).get().value.get
        Logger.debug(res.body)
        if (res.status == 200) {
          val p = """access_token=(.*?)&expires=\d+""".r
          val p(accessToken) = res.body
          Redirect(routes.Application.index).withSession(
              "token" -> accessToken)
        } else BadRequest(res.body)
      }.getOrElse {
        val oauthDialogUrl = "https://www.facebook.com/dialog/oauth?client_id=%s&redirect_uri=%s"
          .format(clientId, redirectUri)
        Redirect(oauthDialogUrl)
      }
    }
  }
}

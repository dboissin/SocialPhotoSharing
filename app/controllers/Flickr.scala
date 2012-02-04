package controllers

import play.api._
import play.api.libs.oauth._
import play.api.mvc._
import oauth.signpost.basic.DefaultOAuthConsumer
import play.api.libs.ws.WS

object Flickr extends Controller {

  val conf = Play.current.configuration
  val key = ConsumerKey(conf.getString("flickr.key").getOrElse(""),
      conf.getString("flickr.secret").getOrElse(""))

  def authenticate = Action { request =>
    request.queryString.get("oauth_verifier").flatMap(_.headOption).map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      Logger.debug(tokenPair.toString)
      val access_token_url = "http://www.flickr.com/services/oauth/access_token?" +
          "oauth_verifier=%s".format(verifier)
      val res = WS.url(access_token_url).sign(OAuthCalculator(key, tokenPair)).get().value.get
      Logger.debug("%s - %s".format(res.status, res.body))

      if (res.status == 200) {
        val p = "fullname=(.*?)&oauth_token=(.*?)&oauth_token_secret=(.*?)&user_nsid=(.*?)&username=(.*?)".r
        val p(fullname, token, secret, nsid, username) = res.body
         Logger.debug("%s - %s - %s - %s - %s".format(fullname, token, secret, nsid, username))
         Redirect(routes.Application.index).withSession("token" -> token, "secret" -> secret)
         .withSession("token" -> token, "secret" -> secret)
      } else {
        BadRequest(res.status.toString)
      }
    }.getOrElse{
       val c = new DefaultOAuthConsumer(key.key, key.secret)
       val res = WS.url(c.sign("http://www.flickr.com/services/oauth/request_token?" +
          "oauth_callback=http%3A%2F%2Flocalhost%3A9000%2Fauth")).get.value.get
       Logger.debug("%s - %s".format(res.status, res.body))
       if (res.status == 200) {
         val p = "oauth_callback_confirmed=(.*?)&oauth_token=(.*?)&oauth_token_secret=(.*?)".r
         val p(confirmed, token, secret) = res.body
         Logger.debug("%s - %s - %s".format(confirmed, token, secret))
         Redirect("http://www.flickr.com/services/oauth/authorize?oauth_token="+token).withSession("token" -> token, "secret" -> secret)
       } else {
         BadRequest(res.status.toString)
       }
    }
  }

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }

  def oAuthCalculator(request: Request[_]) = sessionTokenPair(request) match {
    case Some(tokens) => Some(OAuthCalculator(key, tokens))
    case _ => None
  }

}

package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.oauth._

object Application extends Controller {

  def index = Security.Authenticated(
    request => request.session.get("token"),
    _ => Results.Redirect(routes.Flickr.authenticate))(username => Action(Ok(views.html.index(""))))

  def test = Action { request =>
    Async {
    val tokens = Flickr.sessionTokenPair(request).getOrElse(RequestToken("", ""))
    WS.url("http://api.flickr.com/services/rest/" +
        "?method=flickr.test.login&format=json&nojsoncallback=1")
        .sign(OAuthCalculator(Flickr.key, tokens))
        .get().map{res =>
           Logger.debug("%s - %s".format(res.status, res.json.toString))
           Ok(res.json)
         }
    }
  }
}

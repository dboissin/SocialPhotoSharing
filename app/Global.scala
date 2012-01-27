import play.api._

object Global extends GlobalSettings {

  override def onStart(application:Application) {
    // check db is here
    application.configuration.getString("mongodb.db") match {
      case None => throw new Exception("no db found")
      case _ =>
    }
    Logger.info("Database name : %s".format(application.configuration
        .getString("mongodb.db").getOrElse("machin")))
  }

  override def onStop(application:Application) {

  }

}

import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "SocialPhotoSharing"
    val appVersion      = "1.0"

    val appDependencies = Seq(
       "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT",
       "com.mongodb.casbah" % "casbah-gridfs_2.9.0-1" % "2.1.5.0"
    )


    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    )

}


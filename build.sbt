val scala3Version = "3.7.3"
val circeVersion = "0.14.10"
val ironVersion = "3.2.3"

lazy val root = project
  .in(file("."))
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    name := "claude-statusline-maker",
    version := "0.1.0",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.github.iltotore" %% "iron" % ironVersion,
      "io.github.iltotore" %% "iron-circe" % ironVersion,
      "org.eclipse.jgit" % "org.eclipse.jgit" % "7.5.0.202512021534-r"
    ),
    bashScriptExtraDefines += """addJava "--add-opens=java.base/sun.misc=ALL-UNNAMED"""",
    bashScriptExtraDefines += """addJava "--add-opens=java.base/java.lang=ALL-UNNAMED"""",
    bashScriptExtraDefines += """addJava "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED"""",
    bashScriptExtraDefines += """addJava "--add-opens=java.base/java.util=ALL-UNNAMED"""",
    bashScriptExtraDefines += """addJava "-XX:+IgnoreUnrecognizedVMOptions""""
  )

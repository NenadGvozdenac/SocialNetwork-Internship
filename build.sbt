ThisBuild / scalaVersion := "2.13.11"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """social-network""",
    libraryDependencies ++= Seq(
      guice,
      "mysql" % "mysql-connector-java" % "8.0.33",
      "com.google.inject" % "guice" % "5.1.0",
      "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
      "org.scalamock" %% "scalamock" % "5.2.0" % Test
    )
  )
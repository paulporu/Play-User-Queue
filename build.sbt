name := """play-flexible-user-queue"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.github.paulporu" %% "flexible-user-queue" % "1.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.14",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.10",
  "org.scalatestplus" %% "play" % "1.4.0" % "test"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

fork in run := true

routesGenerator := InjectedRoutesGenerator

name := """squeryl"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
//  "mysql" % "mysql-connector-java" % "latest.release",
  "net.sf.barcode4j" % "barcode4j" % "2.0",
  "org.squeryl" %% "squeryl" % "0.9.5-7",
  "mysql" % "mysql-connector-java" % "5.1.10",
  evolutions
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

routesGenerator := InjectedRoutesGenerator

lazy val myProject = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

fork in run := false
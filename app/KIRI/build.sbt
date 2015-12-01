name := """KIRI"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

//tambah database untuk mysql
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.18"
//untuk pake json
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3"
//untuk pake Java Mail
libraryDependencies += "com.sun.mail" % "javax.mail" % "1.5.4"


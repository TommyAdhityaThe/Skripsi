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
//untuk jbcrypt password
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"


// untuk eclipse
// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)

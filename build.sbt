name := """reactive-auth"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "jquery" % "2.1.3",
  "org.webjars" % "font-awesome" % "4.3.0",
  "org.webjars" % "angularjs" % "1.3.8",
  "com.ning" % "async-http-client" % "1.8.14",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "jp.t2v" %% "play2-auth" % "0.13.2",
  "jp.t2v" %% "play2-auth-test" % "0.13.2" % "test",
  "org.webjars" % "angular-ui-bootstrap" % "0.13.0",
  "org.webjars" % "angular-local-storage" % "0.1.5",
  "org.webjars.bower" % "textAngular" % "1.3.11",
  "org.webjars.bower" % "angular-http-auth" % "1.2.2",
  "org.webjars.bower" % "ng-table" % "0.5.4",
  "org.webjars.bower" % "angular-ui-select" % "0.11.2",
  "org.webjars.bower" % "angular-animate" % "1.4.0",
  "org.webjars.bower" % "angular-ui-router" % "0.2.15",
  "org.webjars.bower" % "angular-growl-v2" % "0.7.5"
)

//specs2 % Test,
//resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator

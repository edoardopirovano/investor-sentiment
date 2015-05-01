scalaVersion := "2.11.6"
libraryDependencies ++= Seq(
	// General logging
	"org.slf4j" % "slf4j-simple" % "1.6.4",
	
	// Date/time handling
	"com.github.nscala-time" %% "nscala-time" % "2.0.0",
	
	// Twitter API
	"org.twitter4j" % "twitter4j-stream" % "4.0.3",
	
	// Guardian API
	"com.gu" %% "content-api-client" % "5.3",
	
	// Configuration loading
	"com.typesafe" % "config" % "1.2.1",
	
	// Database connection
	"mysql" % "mysql-connector-java" % "5.1.12",
  "org.scalikejdbc" %% "scalikejdbc"         % "2.2.6",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "2.2.6"
)
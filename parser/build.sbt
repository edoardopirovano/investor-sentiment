scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
	"org.slf4j" % "slf4j-simple" % "1.6.4",                 // General logging
	"com.github.nscala-time" %% "nscala-time" % "2.0.0", 	// Date/time handling
	"org.twitter4j" % "twitter4j-stream" % "4.0.3",         // Twitter API
	"com.gu" %% "content-api-client" % "5.3",               // Guardian API
	"com.typesafe" % "config" % "1.2.1",                    // Configuration loading
	"mysql" % "mysql-connector-java" % "5.1.12",            // Database connection
	"org.scalikejdbc" %% "scalikejdbc"				 % "2.2.6",
	"org.scalikejdbc" %% "scalikejdbc-config"	 % "2.2.6"
)
name := "goos"

version := "1.0"

resolvers += "Wix Dev Repository" at "http://repo.dev.wix/artifactory/libs-snapshots-local"

libraryDependencies ++= Seq(
  "org.specs2" % "specs2_2.10" % "2.3.12",
  "com.wixpress" %% "specs2-jmock" % "0.1.4-SNAPSHOT",
  "org.hamcrest" % "hamcrest-all" % "1.3",
  "com.googlecode.windowlicker" % "windowlicker-swing" % "r268",
  "org.igniterealtime.smack" % "smack" % "3.2.1",
  "org.igniterealtime.smack" % "smackx" % "3.2.1",
  "org.jmock" % "jmock-junit4" % "2.6.0",
  "commons-io" % "commons-io" % "2.4"
)

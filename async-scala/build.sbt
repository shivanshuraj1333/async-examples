name := "async-scala"
version := "0.1.0"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "4.1.0",
  
  // Explicit SLF4J and Logback dependencies
  "org.slf4j" % "slf4j-api" % "2.0.9",
  "ch.qos.logback" % "logback-classic" % "1.4.11",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

// Assembly configuration
assembly / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*) => MergeStrategy.first
  case "reference.conf" => MergeStrategy.concat
  case x if x.endsWith(".class") => MergeStrategy.last
  case x if x.endsWith(".properties") => MergeStrategy.last
  case _ => MergeStrategy.first
}

// Main class configuration
assembly / mainClass := Some("com.example.Main")

// Ensure all dependencies are packaged
assembly / fullClasspath := (assembly / fullClasspath).value 
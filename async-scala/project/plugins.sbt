// Core plugins
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

// Add scoverage plugin
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")

// Remove native packager and scoverage for now to resolve conflicts 
name := "xsbt-bukkit"

organization := "se.nullable.xsbt-bukkit"

version := "0.0.2-SNAPSHOT"

sbtPlugin := true

scalacOptions += "-deprecation"

resolvers += "Nullable.se" at "http://nexus.nullable.se/nexus/content/groups/public/"

publishMavenStyle := true

publishTo <<= (version) { version: String =>
  val nexus = "http://nexus.nullable.se/nexus/content/repositories/"
  if (version.trim.endsWith("-SNAPSHOT")) Some("snapshots" at nexus + "snapshots/") 
  else                                    Some("releases"  at nexus + "releases/")
}

libraryDependencies += "org.yaml" % "snakeyaml" % "1.10"
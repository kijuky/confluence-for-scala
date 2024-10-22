ThisBuild / resolvers += "Atlassian" at "https://packages.atlassian.com/mvn/maven-atlassian-external/"
val confluenceDependencies = Seq(
  "com.atlassian.confluence" % "confluence-rest-client" % "9.1.0" exclude
    ("com.atlassian.sal", "sal-api"),
  // atlassian-plugin を解決できないので、jarを直接取得する
  "com.atlassian.sal" % "sal-api" % "6.0.4" artifacts
    Artifact("sal-api", "jar", "jar"),
  // Runtime で良いが面倒なので加えておく。
  "javax.xml.bind" % "jaxb-api" % "2.3.1",
  "javax.mail" % "javax.mail-api" % "1.6.2",
  "org.slf4j" % "slf4j-simple" % "1.7.36" % Test
)

lazy val root = project
  .in(file("."))
  .aggregate(vanilla, zio)
  .settings(publish / skip := true)

lazy val vanilla = project
  .in(file("vanilla"))
  .settings(
    name := "confluence-for-scala",
    scalaVersion := "2.12.20", // scala-steward:off
    crossScalaVersions := Seq(scalaVersion.value, "3.3.4"),
    console / initialCommands := "import io.github.kijuky.confluence.Implicits._",
    resolvers += "Atlassian" at "https://packages.atlassian.com/mvn/maven-atlassian-external/",
    libraryDependencies ++= confluenceDependencies ++
      Seq("org.scala-lang.modules" %% "scala-xml" % "2.3.0")
  )

lazy val zio = project
  .in(file("zio"))
  .settings(
    name := "confluence-for-zio",
    scalaVersion := "3.3.4",
    libraryDependencies ++= confluenceDependencies ++
      Seq(
        "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
        "dev.zio" %% "zio" % "2.1.11"
      )
  )

inThisBuild(
  Seq(
    organization := "io.github.kijuky",
    homepage := Some(url("https://github.com/kijuky/confluence-for-scala")),
    licenses := Seq(
      "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "kijuky",
        "Kizuki YASUE",
        "ikuzik@gmail.com",
        url("https://github.com/kijuky")
      )
    ),
    versionScheme := Some("early-semver"),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
  )
)

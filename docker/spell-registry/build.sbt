val scala3Version = "3.2.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

val artifactVersion = sys.env.getOrElse("VERSION", "1.0.0")

lazy val root = project
  .in(file("."))
  .settings(
    name := "spell-registry",
    inThisBuild(List(
      version := artifactVersion,
      organization := "com.ses",
    )),

    scalaVersion := scala3Version,
    dockerExposedPorts ++= Seq(8080),
    dockerBaseImage := "docker-weu.spo.internal.ses.com/eclipse-temurin:19.0.1_10-jre-alpine",

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies ++= Seq(
      "com.goyeau" %% "kubernetes-client" % "0.9.0",
      "com.typesafe" % "config" % "1.4.2",
      "org.http4s" %% "http4s-ember-server" % "0.23.16"
    ),
    dockerUsername := Some("spell"),
    dockerAutoremoveMultiStageIntermediateImages := false,
    dockerAliases := Seq(
      dockerAlias.value.withTag(Option("latest")).withRegistryHost(Option("docker-weu.spo.internal.ses.com")),
      dockerAlias.value.withRegistryHost(Option("docker-weu.spo.internal.ses.com"))
    ),
  )
  .enablePlugins(JavaAppPackaging).enablePlugins(AshScriptPlugin).enablePlugins(DockerPlugin)

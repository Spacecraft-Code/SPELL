package com.ses

import cats.effect.IO
import com.goyeau.kubernetes.client.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import java.io.File
import org.http4s.AuthScheme
import org.http4s.Credentials.Token
import org.http4s.headers.Authorization
import org.http4s.implicits.*
import scala.concurrent.ExecutionContext
import scala.io.Source
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.http4s.Uri
import org.http4s.ParseFailure
import io.k8s.api.core.v1.Service
import io.k8s.api.core.v1.ServiceList
import cats.effect.unsafe.implicits.global
import io.k8s.api.core.v1.Pod
import io.k8s.api.apps.v1.Deployment


object KubeAPI {
  implicit val config: Config = ConfigFactory.load("application")

  val apiServer = Uri.fromString(config.getString("apiserver")) match {
    case Left(ParseFailure(sanitized, details)) => {
      uri"https://kubernetes.default"
    }
    case Right(uri) => uri
  }
  lazy val namespace = Source
                .fromFile("/var/run/secrets/kubernetes.io/serviceaccount/namespace")
                .mkString

  lazy val kubernetesClient =
    KubernetesClient[IO](
      KubeConfig.of[IO](
        server = apiServer,
        authorization = Option(
          Authorization(
            Token(
              AuthScheme.Bearer,
              Source
                .fromFile("/var/run/secrets/kubernetes.io/serviceaccount/token")
                .mkString
            )
          )
        ),
        caCertFile = Option(
          new File("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt")
        )
      )
    )
  def requestHealth() =
    KubeAPI.kubernetesClient != null

  def requestServices(): ServiceList =
    KubeAPI.kubernetesClient.use { client =>
      {
        for {
            serviceList <- client.services.namespace(namespace).list(Map("app.kubernetes.io/name" -> "spell-server"))
        } yield (serviceList)
      }
    }.unsafeRunSync() 

  def getDeployment(name : String) : Deployment =
    KubeAPI.kubernetesClient.use { client =>
      {
        for {
            serviceList <- client.deployments.namespace(namespace).get(name)
        } yield (serviceList)
      }
    }.unsafeRunSync() 

}

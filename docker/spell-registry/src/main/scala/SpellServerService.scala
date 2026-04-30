package com.ses

import com.comcast.ip4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import cats.effect._
import cats.syntax.all._
import fs2.Stream
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.multipart.Multipart
import org.http4s.server._
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator
import org.http4s.syntax.all._
import com.typesafe.config.Config
import scala.util.Try

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

sealed trait SpellMessage
case class SpellError(error: String) extends SpellMessage

case class SpellService(
    name: String,
    mission: String,
    seat: String,
    ip: String,
    contextPort: Int,
    sshPort: Int,
    running: Boolean
)

object SpellServerService {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def serverStream[F[_]: Async](implicit
      config: Config
  ) = {
    val port = Port.fromString(config.getString("server_port")) match {
      case None    => port"8080"
      case Some(p) => p
    }

    EmberServerBuilder.default
      .withPort(port)
      .withHost(host"0.0.0.0")
      .withHttpApp(new SpellServerServiceRoutes[F].routes.orNotFound)
      .withLogger(Slf4jLogger.getLogger[F])
      .build
  }

}

class SpellServerServiceRoutes[F[_]: Sync] extends Http4sDsl[F] {

  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "healthz" => {
        Try {
          val services = KubeAPI.requestHealth().asJson

          Ok(services)
        }.handleError { f =>
          val result = SpellError(s"Failed with ${f}").asJson

          InternalServerError(result)
        }.get
      }

      case req @ GET -> Root / "list" =>
        Try {
          val services = KubeAPI
            .requestServices()
            .items
            .map[SpellService](kubeService =>
              val name = kubeService.metadata.get.name.get
              val labels = kubeService.metadata.get.labels.getOrElse(Map())
              val ip = kubeService.status.fold(None)(status =>
                status.loadBalancer.fold(None)(lb =>
                  lb.ingress.fold(None)(lbi =>
                    if (lbi.isEmpty) None
                    else lbi.get(0).fold(None)(f => f.ip)
                  )
                )
              )
              val port = kubeService.spec.get.ports
                .getOrElse(Seq())
                .find(f => f.name.getOrElse("") == "ssh")
                .fold(None)(p => Some(p.port))
              val deployment = KubeAPI.getDeployment(name)
              SpellService(
                name = name,
                mission = labels
                  .getOrElse("mission", labels.getOrElse("spell-mission", "")),
                seat =
                  labels.getOrElse("seat", labels.getOrElse("spell-seat", "")),
                ip = ip.getOrElse(""),
                contextPort = 19823,
                sshPort = port.getOrElse(-1),
                running = ip.isDefined && port.isDefined && (labels.isDefinedAt(
                  "mission"
                ) || labels.isDefinedAt("spell-mission")) && (labels
                  .isDefinedAt("seat") || labels.isDefinedAt("spell-seat")) && deployment.status.fold(false)(st => st.availableReplicas.fold(false)(_ > 0))
              )
            )

          Ok(services.asJson)
        }.handleError { f =>
          val result = SpellError(s"Failed with ${f}").asJson

          Ok(result)
        }.get

    }
}

package com.ses

import cats.effect.IO
import com.goyeau.kubernetes.client.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import java.io.File
import org.http4s._
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
import org.http4s.HttpRoutes
import cats.effect.ExitCode
import com.ses.SpellServerService
import cats.effect.IOApp

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
implicit val config: Config = ConfigFactory.load("application")


object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    SpellServerService.serverStream[IO].useForever.as(ExitCode.Success)
  }

}

package com.jakfli.fpapi

import com.jakfli.fpapi.HttpServer.HttpServer
import zio.logging.Logging
import zio.stream.ZStream
import zio.{ExitCode, ZIO, _}

object Main extends zio.App {
  val program: RIO[HttpServer with Logging, ExitCode] =
    for {
      _ <- Logging.info("Starting...")
      _ <- ZStream
        .mergeAllUnbounded()(
          ZStream.fromEffect(HttpServer.run.forever)
        )
        .runDrain
        .toManaged_
        .use_(ZIO.succeed(ExitCode.success))
    } yield ExitCode.success

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program
      .provideCustomLayer(Layers.app)
      .catchAll(err =>
        for {
          _        <- Logging
                      .throwable(s"Failed to run program with error: [$err]", err)
                      .provideCustomLayer(Layers.logger)
          exitCode <- ZIO.succeed(ExitCode.failure)
        } yield exitCode
      )
}
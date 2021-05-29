package com.jakfli.fpapi

import zio.logging.Logging
import zio.{ExitCode, ZIO, _}

object Main extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    ZIO.never
      .provideCustomLayer(Layers.app)
      .catchAll(err =>
        for {
          _        <- Logging
                      .throwable(s"Failed to run program with error: [$err]", err)
                      .provideCustomLayer(Layers.logger)
          exitCode <- ZIO.succeed(ExitCode.failure)
        } yield exitCode)
      .exitCode
}
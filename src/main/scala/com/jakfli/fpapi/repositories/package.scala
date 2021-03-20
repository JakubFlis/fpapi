package com.jakfli.fpapi

import cats.effect.Blocker
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import scala.concurrent.ExecutionContext

package object repositories {
  type DatabaseTransactor = Has[Transactor[Task]]

  object DbTransactor {
    private def makeTransactor(
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
    ): Managed[Throwable, Transactor[Task]] =
      HikariTransactor
        .newHikariTransactor[Task](
          "org.postgresql.Driver",
          "jdbc:postgresql://localhost:5432/postgres",
          "",
          "",
          connectEC,
          Blocker.liftExecutionContext(transactEC)
        )
        .toManagedZIO

    val managed: ZManaged[Blocking, Throwable, Transactor[Task]] =
      (for {
        connectEC  <- ZIO.descriptor.map(_.executor.asEC).toManaged_
        blockingEC <- blocking.blocking(ZIO.descriptor.map(_.executor.asEC)).toManaged_
        transactor <- makeTransactor(connectEC, blockingEC)
      } yield transactor)

    val live: ZLayer[Blocking, Throwable, DatabaseTransactor] =
      ZLayer.fromManaged(managed)

    val transactor: URIO[DatabaseTransactor, Transactor[Task]] = ZIO.service
  }
}

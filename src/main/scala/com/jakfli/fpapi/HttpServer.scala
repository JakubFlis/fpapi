package com.jakfli.fpapi

import cats.implicits.catsSyntaxOptionId
import cats.syntax.semigroupk._
import com.jakfli.fpapi.api.{NotificationApi, OrderApi}
import com.jakfli.fpapi.services.NotificationService.NotificationServiceM
import com.jakfli.fpapi.services.OrderService.OrderServiceM
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.syntax.kleisli._
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.clock.Clock
import zio.interop.catz._
import zio.{Has, RIO, RLayer, Runtime, URIO, ZIO, ZLayer, ZManaged}

object HttpServer {
  type RouteEnv      = Has[Clock.Service] with NotificationServiceM with OrderServiceM
  type RequestZIO[A] = RIO[RouteEnv, A]
  type HttpServer    = Has[Server[RequestZIO]]

  val openApiInfo = sttp.tapir.openapi.Info(
    title = "FP API",
    version = "V1",
    description = "jakfli's Blog FP API".some,
    contact = sttp.tapir.openapi.Contact("JakFli".some, "jakub@jakfli.com".some, "https://www.jakfli.com".some).some
  )

  val orderApi       = OrderApi.create()
  val orderRoutes    = orderApi.routes
  val orderEndpoints = orderApi.endpoints

  val notifApi       = NotificationApi.create()
  val notifRoutes    = notifApi.routes
  val notifEndpoints = notifApi.endpoints

  val docs           = OpenAPIDocsInterpreter.serverEndpointsToOpenAPI(
    orderEndpoints ++ notifEndpoints,
    openApiInfo
  )(OpenAPIDocsOptions.default)
  val docRoutes      = new SwaggerHttp4s(docs.toYaml).routes[RequestZIO]
  val routes         = orderRoutes <+> notifRoutes <+> docRoutes

  def createHttp4Server: ZManaged[RouteEnv, Throwable, Server[RequestZIO]] =
    ZManaged.runtime[RouteEnv].flatMap { implicit runtime: Runtime[RouteEnv] =>
            BlazeServerBuilder[RequestZIO](runtime.platform.executor.asEC)
              .bindHttp(8081, "localhost")
              .withHttpApp(Router("/" -> routes).orNotFound)
              .resource
              .toManagedZIO
    }

  val live: RLayer[RouteEnv, HttpServer] =
    ZLayer.fromManaged(createHttp4Server)

  def run: URIO[HttpServer, Server[RequestZIO]] = ZIO.service[Server[RequestZIO]]
}

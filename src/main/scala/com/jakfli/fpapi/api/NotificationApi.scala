package com.jakfli.fpapi.api

import com.jakfli.fpapi.HttpServer.RouteEnv
import com.jakfli.fpapi.api.endpoints.NotificationEndpoints
import com.jakfli.fpapi.api.requests.SendNotificationRequest
import com.jakfli.fpapi.services.NotificationService
import com.jakfli.fpapi.services.NotificationService.{NotificationMessage, NotificationServiceM, NotificationTemplate}
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.ZIO
import zio.interop.catz._

class NotificationApi(val notificationEndpoints: NotificationEndpoints) {
  val placeOrderServerEndpoint: ZServerEndpoint[NotificationServiceM, SendNotificationRequest, String, Unit] =
    notificationEndpoints.sendNotificationEndpoint.zServerLogic(request =>
      NotificationTemplate.convertToTemplate(request.template.value) match {
        case Some(template) =>
          NotificationService
            .sendNotification(request.emailAddress, template, NotificationMessage(request.message.value))
            .bimap(_.toString, _.toString)
        case None =>
          ZIO.fail(s"There's no template [${request.template.value}] in the system.")
      }
    )

  val endpoints = List(
    placeOrderServerEndpoint.widen[RouteEnv]
  )

  val routes = ZHttp4sServerInterpreter
    .from(endpoints)
    .toRoutes
}

object NotificationApi {
  def create(): NotificationApi =
    new NotificationApi(new NotificationEndpoints())
}
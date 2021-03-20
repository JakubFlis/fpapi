package com.jakfli.fpapi.api

import cats.data.NonEmptyList
import com.jakfli.fpapi.api.endpoints.NotificationEndpoints
import com.jakfli.fpapi.api.responses.CreateOrderResponse
import com.jakfli.fpapi.services.CustomerService.Customer.CustomerId
import com.jakfli.fpapi.services.NotificationService
import com.jakfli.fpapi.services.NotificationService.NotificationServiceM
import com.jakfli.fpapi.services.OrderService.Order.{OrderId, OrderItem}
import com.jakfli.fpapi.services.ProductService.Product.ProductId
import com.jakfli.fpapi.services.RestaurantService.Restaurant.RestaurantId
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.ZIO
import zio.interop.catz._

class NotificationApi(val notifEndpoints: NotificationEndpoints) {
  val placeOrderServerEndpoint: ZServerEndpoint[NotificationServiceM, Unit, String, CreateOrderResponse] =
    notifEndpoints.notificationEndpoint.zServerLogic(request =>
      ZIO.succeed(CreateOrderResponse(OrderId(""), CustomerId(""), RestaurantId("res"), NonEmptyList.of(OrderItem(ProductId(""), 1))))
    )

  val endpoints = List(
    placeOrderServerEndpoint
  )

  val routes = ZHttp4sServerInterpreter
    .from(endpoints)
    .toRoutes
}

object NotificationApi {
  def create(): NotificationApi =
    new NotificationApi(new NotificationEndpoints())
}
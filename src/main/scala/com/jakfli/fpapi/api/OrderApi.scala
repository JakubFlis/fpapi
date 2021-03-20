package com.jakfli.fpapi.api

import com.jakfli.fpapi.api.endpoints.OrderEndpoints
import com.jakfli.fpapi.api.requests.CreateOrderRequest
import com.jakfli.fpapi.api.responses.CreateOrderResponse
import com.jakfli.fpapi.services.OrderService
import com.jakfli.fpapi.services.OrderService.{Order, OrderServiceM}
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.interop.catz._

class OrderApi(val orderEndpoints: OrderEndpoints) {
  val placeOrderServerEndpoint: ZServerEndpoint[OrderServiceM, CreateOrderRequest, String, CreateOrderResponse] =
    orderEndpoints.placeOrderEndpoint.zServerLogic(request =>
      OrderService
        .createOrder(Order.fromRequest(request))
        .bimap(_.toString, CreateOrderResponse.fromDomain)
    )

  val endpoints = List(
    placeOrderServerEndpoint
  )

  val routes = ZHttp4sServerInterpreter
    .from(endpoints)
    .toRoutes
}

object OrderApi {
  def create(): OrderApi =
    new OrderApi(new OrderEndpoints())
}
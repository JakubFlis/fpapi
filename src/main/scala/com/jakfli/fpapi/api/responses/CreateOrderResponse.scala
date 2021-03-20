package com.jakfli.fpapi.api.responses

import cats.data.NonEmptyList
import com.jakfli.fpapi.services.CustomerService.Customer.CustomerId
import com.jakfli.fpapi.services.OrderService.Order
import com.jakfli.fpapi.services.OrderService.Order.{OrderId, OrderItem}
import com.jakfli.fpapi.services.RestaurantService.Restaurant.RestaurantId

case class CreateOrderResponse(
  orderId: OrderId,
  consumerId: CustomerId,
  restaurantId: RestaurantId,
  items: NonEmptyList[OrderItem]
)
object CreateOrderResponse {
  def fromDomain(order: Order): CreateOrderResponse =
    CreateOrderResponse(
      order.id,
      order.customerId,
      order.restaurantId,
      order.items
    )
}
package com.jakfli.fpapi.repositories.records

import cats.data.NonEmptyList
import com.jakfli.fpapi.services.CustomerService.Customer.CustomerId
import com.jakfli.fpapi.services.DeliveryService.DeliverySupplier.DeliverySupplierId
import com.jakfli.fpapi.services.OrderService.Order
import com.jakfli.fpapi.services.OrderService.Order.{OrderId, OrderItem, Status}
import com.jakfli.fpapi.services.RestaurantService.Restaurant.RestaurantId
import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant

case class OrderRecord(
  id: OrderId,
  customerId: CustomerId,
  restaurantId: RestaurantId,
  deliverySupplierId: Option[DeliverySupplierId],
  items: NonEmptyList[OrderItem],
  status: Status,
  createdAt: Instant,
  updatedAt: Instant
) {
  def toDomain: Order =
    this
      .transformInto[Order]
}

object OrderRecord {
  def fromDomain(order: Order): OrderRecord =
    order
      .into[OrderRecord]
      .withFieldConst(_.createdAt, Instant.now())
      .withFieldConst(_.updatedAt, Instant.now())
      .transform
}
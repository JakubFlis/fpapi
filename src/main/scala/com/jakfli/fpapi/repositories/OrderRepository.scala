package com.jakfli.fpapi.repositories

import com.jakfli.fpapi.repositories.OrderRepository.SQL
import com.jakfli.fpapi.repositories.records.OrderRecord
import com.jakfli.fpapi.services.DeliveryService.DeliverySupplier.DeliverySupplierId
import com.jakfli.fpapi.services.OrderService.Order.{OrderId, Status}
import doobie.implicits._
import doobie.implicits.legacy.instant._
import doobie.{Query0, Transactor, Update0}
import zio.interop.catz._
import zio.{Has, RLayer, Task}

class OrderRepository(tnx: Transactor[Task]) extends OrderRepository.Service {
  override def getOrder(orderId: OrderId): Task[OrderRecord] =
    SQL
      .getOrder(orderId)
      .stream
      .compile
      .lastOrError
      .transact(tnx)

  override def createOrder(order: OrderRecord): Task[OrderRecord] =
    SQL
      .createOrder(order)
      .run
      .transact(tnx)
      .as(order)

  override def changeOrderStatus(orderId: OrderId, status: Status): Task[OrderRecord] =
    SQL
      .updateOrderStatus(orderId, status)
      .transact(tnx)

  override def assignSupplierToOrder(orderId: OrderId, deliverySupplierId: DeliverySupplierId): Task[OrderRecord] =
    SQL
      .updateSupplierId(orderId, deliverySupplierId)
      .transact(tnx)
}

object OrderRepository {
  type OrderPersistence = Has[Service]

  trait Service {
    def getOrder(orderId: OrderId): Task[OrderRecord]
    def createOrder(order: OrderRecord): Task[OrderRecord]
    def changeOrderStatus(orderId: OrderId, status: Status): Task[OrderRecord]
    def assignSupplierToOrder(orderId: OrderId, deliverySupplierId: DeliverySupplierId): Task[OrderRecord]
  }

  val live: RLayer[DatabaseTransactor, OrderPersistence] =
    DbTransactor
      .transactor
      .map(new OrderRepository(_))
      .toLayer

  object SQL {
    import DoobieMeta._

    def getOrder(id: OrderId): Query0[OrderRecord] =
      sql"""
          SELECT * FROM orders WHERE id = ${id.value}
         """.query[OrderRecord]

    def createOrder(order: OrderRecord): Update0 =
      sql"""
          INSERT INTO orders (
            id,
            customer_id,
            restaurant_id,
            supplier_id,
            status,
            created_at,
            updated_at
          ) VALUES (
            ${order.id},
            ${order.customerId},
            ${order.restaurantId},
            ${order.deliverySupplierId},
            ${order.createdAt},
            ${order.updatedAt}
          )
        """.update

    def updateOrderStatus(id: OrderId, status: Status): doobie.ConnectionIO[OrderRecord] =
      sql"""
            UPDATE orders SET status = $status, updated_at = now()
            WHERE id = ${id.value}
         """
          .update
          .withUniqueGeneratedKeys[OrderRecord](
            "id",
            "customer_id",
            "restaurant_id",
            "supplier_id",
            "status",
            "created_at",
            "updated_at"
          )

    def updateSupplierId(id: OrderId, supplierId: DeliverySupplierId): doobie.ConnectionIO[OrderRecord] =
      sql"""
           UPDATE orders SET supplier = ${supplierId.value}
           WHERE id = ${id.value}
         """
        .update
        .withUniqueGeneratedKeys[OrderRecord](
          "id",
          "customer_id",
          "restaurant_id",
          "supplier_id",
          "status",
          "created_at",
          "updated_at"
        )
  }
}

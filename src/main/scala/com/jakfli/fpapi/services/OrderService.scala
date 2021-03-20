package com.jakfli.fpapi.services

import cats.data.NonEmptyList
import com.jakfli.fpapi.api.requests.CreateOrderRequest
import com.jakfli.fpapi.repositories.OrderRepository.OrderPersistence
import com.jakfli.fpapi.repositories.RestaurantRepository.RestaurantPersistence
import com.jakfli.fpapi.repositories.records.OrderRecord
import com.jakfli.fpapi.repositories.{OrderRepository, RestaurantRepository}
import com.jakfli.fpapi.services.CustomerService.Customer.CustomerId
import com.jakfli.fpapi.services.DeliveryService.DeliverySupplier.DeliverySupplierId
import com.jakfli.fpapi.services.NotificationService.{OrderPlaced, _}
import com.jakfli.fpapi.services.OrderService.Order
import com.jakfli.fpapi.services.OrderService.Order._
import com.jakfli.fpapi.services.ProductService.Product.ProductId
import com.jakfli.fpapi.services.RestaurantService.Restaurant.RestaurantId
import enumeratum.EnumEntry.UpperSnakecase
import zio.logging.Logger
import zio.{Has, RIO, Task, URLayer, ZIO}

import java.util.UUID

class OrderService(
  log: Logger[String],
  orderRepo: OrderRepository.Service,
  restaurantRepo: RestaurantRepository.Service,
  notificationService: NotificationService.Service
) extends OrderService.Service {
  override def placeOrder(order: Order): Task[Order] =
    for {
      _            <- log.info(s"Placing a new Order with ID [${order.id.value}]")
      createdOrder <- orderRepo.createOrder(OrderRecord.fromDomain(order))
      restaurant   <- restaurantRepo.getRestaurant(order.restaurantId)
      message      <- Task.succeed(s"An order with items [${order.items}] has been placed. Please check your Account Dashboard for more information.")
      _            <- notificationService.sendNotification(restaurant.address.emailAddress, OrderPlaced, message.asNotificationMessage)
    } yield createdOrder.toDomain

  override def assignToSupplier(orderId: OrderId, deliverySupplierId: DeliverySupplierId): Task[Order] =
    for {
      _            <- log.info(s"Assigning Delivery Supplier ${deliverySupplierId.value} to Order ${orderId.value}")
      updatedOrder <- orderRepo.assignSupplierToOrder(orderId, deliverySupplierId)
    } yield updatedOrder.toDomain

  override def changeStatus(orderId: OrderId, status: Status): Task[Order] =
    for {
      order        <- orderRepo.getOrder(orderId)
      _            <- log.info(s"Changing Order status [${orderId.value}] from ${order.status} to $status")
      updatedOrder <- orderRepo.changeOrderStatus(orderId, status)
    } yield updatedOrder.toDomain
}

object OrderService {
  type OrderServiceM    = Has[Service]
  type OrderServiceEnv  = NotificationServiceM
    with RestaurantPersistence
    with OrderPersistence
    with Has[Logger[String]]

  trait Service {
    def placeOrder(order: Order): Task[Order]
    def assignToSupplier(orderId: OrderId, deliverySupplierId: DeliverySupplierId): Task[Order]
    def changeStatus(orderId: OrderId, status: Status): Task[Order]
  }

  def createOrder(order: Order): RIO[OrderServiceM, Order] =
    ZIO.accessM(_.get.placeOrder(order))

  def changeStatus(orderId: OrderId, status: Status): RIO[OrderServiceM, Order] =
    ZIO.accessM(_.get.changeStatus(orderId, status))

  def assignToSupplier(orderId: OrderId, deliverySupplierId: DeliverySupplierId): RIO[OrderServiceM, Order] =
    ZIO.accessM(_.get.assignToSupplier(orderId, deliverySupplierId))

  val live: URLayer[OrderServiceEnv, OrderServiceM] =
    (for {
      logger                <- ZIO.service[Logger[String]]
      orderRepository       <- ZIO.service[OrderRepository.Service]
      restaurantRepository  <- ZIO.service[RestaurantRepository.Service]
      notificationService   <- ZIO.service[NotificationService.Service]
    } yield new OrderService(
      logger,
      orderRepository,
      restaurantRepository,
      notificationService
    )).toLayer

  case class Order(
    id: OrderId,
    customerId: CustomerId,
    restaurantId: RestaurantId,
    deliverySupplierId: Option[DeliverySupplierId],
    items: NonEmptyList[OrderItem],
    status: Status
  )

  object Order {
    import enumeratum._

    sealed trait Status extends EnumEntry with UpperSnakecase

    object Status extends Enum[Status] with DoobieEnum[Status] {
      case object Created            extends Status
      case object Rejected           extends Status
      case object InProgress         extends Status
      case object WaitingForPickup   extends Status
      case object InDelivery         extends Status
      case object Delivered          extends Status
      case object Cancelled          extends Status

      val values: IndexedSeq[Status] = findValues
    }

    def fromRequest(
      request: CreateOrderRequest
    ): Order =
      new Order(
        OrderId.generateRandom(),
        request.consumerId,
        request.restaurantId,
        None,
        request.items,
        Status.Created
      )

    case class OrderId(value: String) extends AnyVal
    object OrderId {
      def generateRandom(): OrderId =
        OrderId(UUID.randomUUID().toString)
    }

    case class OrderItem(productId: ProductId, quantity: Int)
  }
}

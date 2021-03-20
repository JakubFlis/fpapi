package com.jakfli.fpapi.services

import com.jakfli.fpapi.models.{Address, PartyIdentity}
import com.jakfli.fpapi.repositories.DeliveryRepository
import com.jakfli.fpapi.repositories.records.DeliverySupplierRecord
import com.jakfli.fpapi.services.DeliveryService.DeliverySupplier._
import enumeratum.EnumEntry.UpperSnakecase
import zio.logging.Logger
import zio.{Has, Task, RIO, ZIO}

import java.util.UUID

class DeliveryService(deliveryRepo: DeliveryRepository.Service, log: Logger[String]) extends DeliveryService.Service {
  override def listAvailableSuppliers(): Task[List[DeliveryService.DeliverySupplier]] =
    for {
      _         <- log.info("Listing all suppliers with Available status...")
      resultRec <- deliveryRepo.listSuppliersForStatus(Status.Available)
      result    <- Task.succeed(resultRec.map(_.toDomain))
    } yield result

  override def registerDeliverySupplier(deliverySupplier: DeliveryService.DeliverySupplier): Task[DeliveryService.DeliverySupplier] =
    for {
      _       <- log.info(s"Registering new Delivery Supplier [${deliverySupplier.id}]")
      result  <- deliveryRepo.createDeliverySupplier(DeliverySupplierRecord.fromDomain(deliverySupplier))
    } yield result.toDomain

  override def changeStatus(deliverySupplierId: DeliverySupplierId, status: Status): Task[DeliveryService.DeliverySupplier] =
    for {
      _       <- log.info(s"Changing status of Delivery Supplier with ID [$deliverySupplierId] to $status")
      result  <- deliveryRepo.changeDeliverySupplierStatus(deliverySupplierId, status)
    } yield result.toDomain
}

object DeliveryService {
  type DeliveryServiceM = Has[Service]

  trait Service {
    def listAvailableSuppliers(): Task[List[DeliverySupplier]]
    def registerDeliverySupplier(deliverySupplier: DeliverySupplier): Task[DeliverySupplier]
    def changeStatus(deliverySupplierId: DeliverySupplierId, status: Status): Task[DeliverySupplier]
  }

  def listAvailableSuppliers(): RIO[DeliveryServiceM, List[DeliverySupplier]] =
    ZIO.accessM(_.get.listAvailableSuppliers())

  def registerDeliverySupplier(deliverySupplier: DeliverySupplier): RIO[DeliveryServiceM, DeliverySupplier] =
    ZIO.accessM(_.get.registerDeliverySupplier(deliverySupplier))

  def changeStatus(deliverySupplierId: DeliverySupplierId, status: Status): RIO[DeliveryServiceM, DeliverySupplier] =
    ZIO.accessM(_.get.changeStatus(deliverySupplierId, status))

  case class DeliverySupplier(
    id: DeliverySupplierId,
    identity: PartyIdentity,
    address: Address,
    status: Status,
    mobilityType: MobilityType
  )
  object DeliverySupplier {
    import enumeratum._

    case class DeliverySupplierId(value: String) extends AnyVal
    object DeliverySupplierId {
      def generateRandom(): DeliverySupplierId =
        DeliverySupplierId(UUID.randomUUID().toString)
    }

    sealed trait Status extends EnumEntry with UpperSnakecase
    object Status extends Enum[Status] {
      case object Available extends Status
      case object Offline   extends Status
      case object Occupied  extends Status

      val values: IndexedSeq[Status] = findValues
    }

    sealed trait MobilityType extends EnumEntry with UpperSnakecase
    object MobilityType extends Enum[MobilityType] {
      case object Bike    extends MobilityType
      case object Car     extends MobilityType
      case object Scooter extends MobilityType

      val values: IndexedSeq[MobilityType] = findValues
    }
  }
}

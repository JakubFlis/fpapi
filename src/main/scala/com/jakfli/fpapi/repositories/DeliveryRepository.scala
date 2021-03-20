package com.jakfli.fpapi.repositories

import com.jakfli.fpapi.repositories.records.DeliverySupplierRecord
import com.jakfli.fpapi.services.DeliveryService.DeliverySupplier.{DeliverySupplierId, Status}
import zio.{Has, Task}

object DeliveryRepository {
  type DeliveryPersistence = Has[Service]

  trait Service {
    def createDeliverySupplier(deliverySupplierRecord: DeliverySupplierRecord): Task[DeliverySupplierRecord]
    def changeDeliverySupplierStatus(deliverySupplierId: DeliverySupplierId, status: Status): Task[DeliverySupplierRecord]
    def listSuppliersForStatus(status: Status): Task[List[DeliverySupplierRecord]]
  }
}

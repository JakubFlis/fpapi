package com.jakfli.fpapi.repositories.records

import com.jakfli.fpapi.models.{Address, PartyIdentity}
import com.jakfli.fpapi.services.DeliveryService.DeliverySupplier
import com.jakfli.fpapi.services.DeliveryService.DeliverySupplier._

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant

case class DeliverySupplierRecord (
  id: DeliverySupplierId,
  identity: PartyIdentity,
  address: Address,
  status: Status,
  mobilityType: MobilityType,
  createdAt: Instant,
  updatedAt: Instant
) {
  def toDomain: DeliverySupplier =
    this
      .transformInto[DeliverySupplier]
}

object DeliverySupplierRecord {
  def fromDomain(deliverySupplier: DeliverySupplier): DeliverySupplierRecord =
    deliverySupplier
      .into[DeliverySupplierRecord]
      .withFieldConst(_.createdAt, Instant.now())
      .withFieldConst(_.updatedAt, Instant.now())
      .transform
}
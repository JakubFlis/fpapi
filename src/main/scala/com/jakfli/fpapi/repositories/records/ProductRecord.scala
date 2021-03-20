package com.jakfli.fpapi.repositories.records

import com.jakfli.fpapi.services.ProductService.Product._
import com.jakfli.fpapi.services.ProductService.{Product => DomainProduct}
import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant

case class ProductRecord(
  id: ProductId,
  name: Name,
  price: Price,
  description: Option[Description],
  availability: Availability,
  createdAt: Instant,
  updatedAt: Instant
) {
  def toDomain: DomainProduct =
    this
      .transformInto[DomainProduct]
}

object ProductRecord {
  def fromDomain(product: DomainProduct): ProductRecord =
    product
      .into[ProductRecord]
      .withFieldConst(_.createdAt, Instant.now())
      .withFieldConst(_.updatedAt, Instant.now())
      .transform
}

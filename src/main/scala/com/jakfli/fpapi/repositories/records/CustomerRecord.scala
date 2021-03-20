package com.jakfli.fpapi.repositories.records

import com.jakfli.fpapi.models.{Address, PartyIdentity}
import com.jakfli.fpapi.services.CustomerService.Customer
import com.jakfli.fpapi.services.CustomerService.Customer._

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant

case class CustomerRecord (
  id: CustomerId,
  identity: PartyIdentity,
  defaultAddress: Address,
  createdAt: Instant,
  updatedAt: Instant
)

object CustomerRecord {
  def fromDomain(customer: Customer): CustomerRecord =
    customer
      .into[CustomerRecord]
      .withFieldConst(_.createdAt, Instant.now())
      .withFieldConst(_.updatedAt, Instant.now())
      .transform
}
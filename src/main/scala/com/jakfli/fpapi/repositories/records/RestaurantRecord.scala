package com.jakfli.fpapi.repositories.records

import com.jakfli.fpapi.models.{Address, PartyIdentity}
import com.jakfli.fpapi.services.RestaurantService.Restaurant
import com.jakfli.fpapi.services.RestaurantService.Restaurant.{OpenStatus, RestaurantId}

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant

case class RestaurantRecord(
  id: RestaurantId,
  address: Address,
  identity: PartyIdentity,
  openStatus: OpenStatus,
  createdAt: Instant,
  updatedAt: Instant
) {
  def toDomain: Restaurant =
    this
      .transformInto[Restaurant]
}

object RestaurantRecord {
  def fromDomain(restaurant: Restaurant): RestaurantRecord =
    restaurant
      .into[RestaurantRecord]
      .withFieldConst(_.createdAt, Instant.now())
      .withFieldConst(_.updatedAt, Instant.now())
      .transform
}
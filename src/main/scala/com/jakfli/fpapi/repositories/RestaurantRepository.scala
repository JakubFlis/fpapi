package com.jakfli.fpapi.repositories

import com.jakfli.fpapi.repositories.RestaurantRepository.SQL
import com.jakfli.fpapi.repositories.records.RestaurantRecord
import com.jakfli.fpapi.services.RestaurantService.Restaurant.{OpenStatus, RestaurantId}
import doobie.implicits._
import doobie.implicits.legacy.instant._
import doobie.{Query0, Transactor, Update0}
import zio.interop.catz._
import zio.{Has, RLayer, Task}

class RestaurantRepository(tnx: Transactor[Task]) extends RestaurantRepository.Service {
  override def getRestaurant(restaurantId: RestaurantId): Task[RestaurantRecord] =
    SQL
      .getRestaurant(restaurantId)
      .stream
      .compile
      .lastOrError
      .transact(tnx)

  override def createRestaurant(restaurant: RestaurantRecord): Task[RestaurantRecord] =
    SQL
      .createRestaurant(restaurant)
      .run
      .transact(tnx)
      .as(restaurant)

  override def getRestaurantsWithStatus(status: OpenStatus): Task[List[RestaurantRecord]] =
    SQL
      .getRestaurantWithStatus(status)
      .stream
      .compile
      .toList
      .transact(tnx)

  override def changeRestaurantOpenStatus(restaurantId: RestaurantId, status: OpenStatus): Task[RestaurantRecord] =
    SQL
      .updateRestaurantOpenStatus(restaurantId, status)
      .transact(tnx)
}

object RestaurantRepository {
  type RestaurantPersistence = Has[Service]

  trait Service {
    def getRestaurant(restaurantId: RestaurantId): Task[RestaurantRecord]
    def createRestaurant(restaurant: RestaurantRecord): Task[RestaurantRecord]
    def getRestaurantsWithStatus(status: OpenStatus): Task[List[RestaurantRecord]]
    def changeRestaurantOpenStatus(restaurantId: RestaurantId, status: OpenStatus): Task[RestaurantRecord]
  }

  val live: RLayer[DatabaseTransactor, RestaurantPersistence] =
    DbTransactor
      .transactor
      .map(new RestaurantRepository(_))
      .toLayer

  object SQL {
    import DoobieMeta._

    def getRestaurant(id: RestaurantId): Query0[RestaurantRecord] =
      sql"""
          SELECT * FROM restaurants WHERE id = ${id.value}
         """.query[RestaurantRecord]

    def createRestaurant(restaurant: RestaurantRecord): Update0 =
      sql"""
          INSERT INTO restaurants (
            id,
            open_status,
            address,
            identity,
            created_at,
            updated_at
          ) VALUES (
            ${restaurant.id},
            ${restaurant.openStatus},
            ${restaurant.address},
            ${restaurant.identity},
            ${restaurant.createdAt},
            ${restaurant.updatedAt}
          )
         """.update

    def getRestaurantWithStatus(status: OpenStatus): Query0[RestaurantRecord] =
      sql"""
          SELECT * FROM restaurants WHERE open_status = $status
         """.query[RestaurantRecord]

    def updateRestaurantOpenStatus(id: RestaurantId, status: OpenStatus) =
      sql"""
          UPDATE restaurants SET status = $status, updated_at = now()
          WHERE id = ${id.value}
         """
        .update
        .withUniqueGeneratedKeys[RestaurantRecord](
          "id",
          "address",
          "identity",
          "open_status",
          "created_at",
          "updated_at"
        )
  }
}

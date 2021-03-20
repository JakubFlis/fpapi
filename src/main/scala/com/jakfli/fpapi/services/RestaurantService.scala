package com.jakfli.fpapi.services

import com.jakfli.fpapi.models.{Address, PartyIdentity}
import com.jakfli.fpapi.services.RestaurantService.Restaurant._
import com.jakfli.fpapi.repositories.RestaurantRepository
import com.jakfli.fpapi.repositories.records.RestaurantRecord
import com.jakfli.fpapi.services.RestaurantService.Restaurant.OpenStatus.Open
import com.jakfli.fpapi.services.RestaurantService._
import enumeratum.EnumEntry.UpperSnakecase
import zio.logging.Logger
import zio.{Has, RIO, Task, URIO, ZIO}

import java.util.UUID

class RestaurantService(log: Logger[String], restaurantRepo: RestaurantRepository.Service) extends RestaurantService.Service {
  override def listAvailableRestaurants(): Task[List[Restaurant]] =
    for {
      _                    <- log.debug(s"Listing available Restaurants...")
      availableRestaurants <- restaurantRepo.getRestaurantsWithStatus(Open)
    } yield availableRestaurants.map(_.toDomain)

  override def createRestaurant(restaurant: Restaurant): Task[Restaurant] =
    for {
      _                 <- log.info(s"Creating new Restaurant ${restaurant.id.value}")
      createdRestaurant <- restaurantRepo.createRestaurant(RestaurantRecord.fromDomain(restaurant))
    } yield createdRestaurant.toDomain

  override def changeOpenStatus(restaurantId: RestaurantId, openStatus: OpenStatus): Task[Restaurant] =
    for {
      restaurant <- restaurantRepo.getRestaurant(restaurantId)
      _          <- ZIO.cond(
                      restaurant.openStatus != openStatus,
                      ZIO.unit,
                      new Throwable(s"Restaurant ${restaurantId.value} open status is already [$openStatus]")
                    )
      _          <- log.info(s"Changing Restaurant [${restaurantId.value}] open status from [${restaurant.openStatus}] to [$openStatus]")
      updated    <- restaurantRepo.changeRestaurantOpenStatus(restaurantId, openStatus)
    } yield updated.toDomain
}

object RestaurantService {
  type RestaurantServiceM = Has[Service]

  trait Service {
    def listAvailableRestaurants(): Task[List[Restaurant]]
    def createRestaurant(restaurant: Restaurant): Task[Restaurant]
    def changeOpenStatus(restaurantId: RestaurantId, openStatus: OpenStatus): Task[Restaurant]
  }

  def createRestaurant(restaurant: Restaurant): RIO[RestaurantServiceM, Restaurant] =
    ZIO.accessM(_.get.createRestaurant(restaurant))

  def listAvailableRestaurants(): RIO[RestaurantServiceM, List[Restaurant]] =
    ZIO.accessM(_.get.listAvailableRestaurants())

  def changeOpenStatus(restaurantId: RestaurantId, openStatus: OpenStatus): RIO[RestaurantServiceM, Restaurant] =
    ZIO.accessM(_.get.changeOpenStatus(restaurantId, openStatus))

  case class Restaurant(
    id: RestaurantId,
    address: Address,
    identity: PartyIdentity,
    openStatus: OpenStatus
  )

  object Restaurant {
    import enumeratum._

    case class RestaurantId(value: String) extends AnyVal
    object RestaurantId {
      def generateRandom(): RestaurantId =
        RestaurantId(UUID.randomUUID().toString)
    }

    sealed trait OpenStatus extends EnumEntry with UpperSnakecase

    object OpenStatus extends Enum[OpenStatus] with DoobieEnum[OpenStatus] {
      case object Open   extends OpenStatus
      case object Closed extends OpenStatus

      val values: IndexedSeq[OpenStatus] = findValues
    }
  }
}

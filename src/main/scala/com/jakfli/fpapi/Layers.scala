package com.jakfli.fpapi

import com.jakfli.fpapi.repositories.{DbTransactor, OrderRepository, RestaurantRepository}
import com.jakfli.fpapi.services.{NotificationService, OrderService}
import zio.ULayer
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object Layers {
  val logger: ULayer[Logging] =
    Slf4jLogger.makeWithAllAnnotationsAsMdc()

  /* PERSISTENCE */
  val databaseTransactor    = Blocking.live >>> DbTransactor.live
  val orderPersistence      = databaseTransactor >>> OrderRepository.live
  val restaurantPersistence = databaseTransactor >>> RestaurantRepository.live

  /* SERVICES */
  val notificationService = {
    logger >>>
      NotificationService.live
  }
  val orderService = {
    (notificationService ++ restaurantPersistence ++ orderPersistence ++ logger) >>>
      OrderService.live
  }

  val httpServer = {
    (Clock.live ++ notificationService ++ orderService) >>>
      HttpServer.live
  }

  val app = httpServer ++ logger
}

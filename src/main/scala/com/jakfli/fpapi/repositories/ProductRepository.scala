package com.jakfli.fpapi.repositories

import com.jakfli.fpapi.repositories.records.ProductRecord
import com.jakfli.fpapi.services.ProductService.Product.{Availability, ProductId}
import com.jakfli.fpapi.services.RestaurantService.Restaurant.RestaurantId
import zio.{Has, Task}

object ProductRepository {
  type ProductPersistence = Has[Service]

  trait Service {
    def getProductsWithAvailability(availability: Availability): Task[List[ProductRecord]]
    def createProduct(product: ProductRecord): Task[ProductRecord]
    def getProductsForRestaurant(restaurantId: RestaurantId): Task[List[ProductRecord]]
    def setProductAvailability(productId: ProductId, availability: Availability): Task[ProductRecord]
  }
}

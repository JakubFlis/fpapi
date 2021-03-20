package com.jakfli.fpapi.services

import com.jakfli.fpapi.models.MoneyMinor
import com.jakfli.fpapi.repositories.ProductRepository
import com.jakfli.fpapi.repositories.records.ProductRecord
import com.jakfli.fpapi.services.ProductService.Product.Availability.Available
import com.jakfli.fpapi.services.ProductService.Product._
import com.jakfli.fpapi.services.ProductService.{Product => DomainProduct}
import com.jakfli.fpapi.services.RestaurantService.Restaurant.RestaurantId
import enumeratum.EnumEntry.UpperSnakecase
import zio.logging.Logger
import zio.{Has, Task, RIO, ZIO}

import java.util.UUID

class ProductService(
  log: Logger[String],
  productRepo: ProductRepository.Service
) extends ProductService.Service {
  override def listAvailableProducts(): Task[List[DomainProduct]] =
    for {
      _        <- log.debug(s"Listing available Products...")
      products <- productRepo.getProductsWithAvailability(Available)
    } yield products.map(_.toDomain)

  override def createProduct(product: DomainProduct): Task[DomainProduct] =
    for {
      _              <- log.info(s"Creating new Product with ID ${product.id.value}...")
      createdProduct <- productRepo.createProduct(ProductRecord.fromDomain(product))
    } yield createdProduct.toDomain

  override def listRestaurantProducts(restaurantId: RestaurantId): Task[List[DomainProduct]] =
    for {
      _         <- log.debug(s"Listing Products for Restaurant ID [${restaurantId.value}]")
      products  <- productRepo.getProductsForRestaurant(restaurantId)
    } yield products.map(_.toDomain)

  override def setProductAvailability(productId: ProductId, availability: Availability): Task[DomainProduct] =
    for {
      _              <- log.info(s"Setting Product [${productId.value}] availability as [$availability]")
      updatedProduct <- productRepo.setProductAvailability(productId, availability)
    } yield updatedProduct.toDomain
}

object ProductService {
  type ProductServiceM = Has[Service]

  trait Service {
    def listAvailableProducts(): Task[List[Product]]
    def createProduct(product: Product): Task[Product]
    def listRestaurantProducts(restaurantId: RestaurantId): Task[List[Product]]
    def setProductAvailability(productId: ProductId, availability: Availability): Task[Product]
  }

  def createProduct(product: Product): RIO[ProductServiceM, Product] =
    ZIO.accessM(_.get.createProduct(product))

  def listAvailableProducts(): RIO[ProductServiceM, List[Product]] =
    ZIO.accessM(_.get.listAvailableProducts())

  def listRestaurantProducts(restaurantId: RestaurantId): RIO[ProductServiceM, List[Product]] =
    ZIO.accessM(_.get.listRestaurantProducts(restaurantId))

  def setProductAvailability(productId: ProductId, availability: Availability): RIO[ProductServiceM, Product] =
    ZIO.accessM(_.get.setProductAvailability(productId, availability))

  case class Product(
    id: ProductId,
    name: Name,
    price: Price,
    description: Option[Description],
    availability: Availability
  )

  object Product {
    import enumeratum._

    def create(
      name: Name,
      price: Price,
      description: Option[Description],
      availability: Availability
    ): Product =
      new Product(ProductId.generateRandom(), name, price, description, availability)

    case class ProductId(value: String)   extends AnyVal
    object ProductId {
      def generateRandom(): ProductId =
        ProductId(UUID.randomUUID().toString)
    }

    case class Name(value: String)        extends AnyVal
    case class Price(value: MoneyMinor)   extends AnyVal
    case class Description(value: String) extends AnyVal

    sealed trait Availability extends EnumEntry with UpperSnakecase
    object Availability extends Enum[Availability] {
      case object Available  extends Availability
      case object OutOfStock extends Availability

      val values: IndexedSeq[Availability] = findValues
    }
  }
}

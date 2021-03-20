package com.jakfli.fpapi.repositories

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode
import cats.data.NonEmptyList
import com.jakfli.fpapi.models.{Address, PartyIdentity}
import com.jakfli.fpapi.services.DeliveryService.DeliverySupplier.DeliverySupplierId
import com.jakfli.fpapi.services.OrderService.Order.OrderItem
import com.jakfli.fpapi.services.ProductService.Product.ProductId
import doobie.util._
import doobie.util.meta.Meta

object DoobieMeta {
  implicit val orderItems: Meta[NonEmptyList[OrderItem]] =
    new Meta[NonEmptyList[OrderItem]](
      Get[String].temap { storedValue =>
        val split = storedValue
          .split(",")
          .map(_.split("%%"))
          .filter(_.length == 2)
          .map(arrayItem => OrderItem(
            ProductId(arrayItem(0)),
            arrayItem(1).toInt
          ))
        NonEmptyList
          .fromList(split.toList)
          .toRight(s"Converted OrderItem list is not empty [$storedValue]")
      },
      Put[String].contramap(_.foldLeft("") { (acc, value) =>
        s"$acc,${value.productId.value}%%${value.quantity}"
      }
      )
    )

  implicit val deliverySupplierIdMeta: Meta[DeliverySupplierId] =
    new Meta[DeliverySupplierId](
      Get[String].temap(value =>
        Either.cond(
          value.nonEmpty,
          DeliverySupplierId(value),
          s"Value $value is empty"
        )
      ),
      Put[String].contramap(_.value)
    )

  implicit val addressMeta: Meta[Address] =
    new Meta[Address](
      Get[String].temap(stringValue => decode[Address](stringValue).left.map(_.toString)),
      Put[String].contramap(_.asJson.noSpaces)
    )

  implicit val identityMeta: Meta[PartyIdentity] =
    new Meta[PartyIdentity](
      Get[String].temap(stringValue => decode[PartyIdentity](stringValue).left.map(_.toString)),
      Put[String].contramap(_.asJson.noSpaces)
    )
}

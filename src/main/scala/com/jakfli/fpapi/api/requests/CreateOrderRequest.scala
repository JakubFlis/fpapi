package com.jakfli.fpapi.api.requests

import cats.data.NonEmptyList
import com.jakfli.fpapi.models.Address
import com.jakfli.fpapi.services.CustomerService.Customer.CustomerId
import com.jakfli.fpapi.services.OrderService.Order.OrderItem
import com.jakfli.fpapi.services.RestaurantService.Restaurant.RestaurantId

case class CreateOrderRequest(
   consumerId: CustomerId,
   restaurantId: RestaurantId,
   items: NonEmptyList[OrderItem],
   deliveryAddress: Address
)

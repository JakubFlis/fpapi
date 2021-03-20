package com.jakfli.fpapi.services

import com.jakfli.fpapi.models.{Address, PartyIdentity}
import com.jakfli.fpapi.repositories.CustomerRepository
import com.jakfli.fpapi.repositories.records.CustomerRecord
import com.jakfli.fpapi.services.CustomerService.Customer
import com.jakfli.fpapi.services.CustomerService.Customer._
import zio.logging.Logger
import zio.{Has, Task, RIO, ZIO}

import java.util.UUID

class CustomerService(customerRepo: CustomerRepository.Service, log: Logger[String]) extends CustomerService.Service {
  override def registerConsumer(customer: Customer): Task[CustomerService.Customer] =
    for {
      _  <- log.info(s"Registering customer [${customer.id}]")
      _  <- customerRepo.createCustomer(CustomerRecord.fromDomain(customer))
    } yield customer
}

object CustomerService {
  type CustomerServiceM = Has[Service]

  trait Service {
    def registerConsumer(customer: Customer): Task[Customer]
  }

  def registerCustomer(customer: Customer): RIO[CustomerServiceM, Customer] =
    ZIO.accessM(_.get.registerConsumer(customer))

  case class Customer(
    id: CustomerId,
    identity: PartyIdentity,
    defaultAddress: Address
  )

  object Customer {
    case class CustomerId(value: String) extends AnyVal
    object CustomerId {
      def generateRandom(): CustomerId =
        CustomerId(UUID.randomUUID().toString)
    }
  }
}

package com.jakfli.fpapi.repositories

import com.jakfli.fpapi.repositories.records.CustomerRecord
import zio.{Has, Task}

object CustomerRepository {
  type CustomerPersistence = Has[Service]

  trait Service {
    def createCustomer(customerRecord: CustomerRecord): Task[CustomerRecord]
  }
}

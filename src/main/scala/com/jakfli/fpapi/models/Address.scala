package com.jakfli.fpapi.models

import com.jakfli.fpapi.models.Address._

case class Address(
  street: Street,
  streetExtra1: Option[Street],
  streetExtra2: Option[Street],
  zipCode: ZipCode,
  city: City,
  country: Country,
  phoneNumber: PhoneNumber,
  emailAddress: EmailAddress
)

object Address {
  case class ZipCode(value: String)      extends AnyVal
  case class City(value: String)         extends AnyVal
  case class Country(value: String)      extends AnyVal
  case class Street(value: String)       extends AnyVal
  case class PhoneNumber(value: String)  extends AnyVal
  case class EmailAddress(value: String) extends AnyVal
}
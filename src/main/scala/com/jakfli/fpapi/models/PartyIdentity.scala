package com.jakfli.fpapi.models

import com.jakfli.fpapi.models.PartyIdentity._

case class PartyIdentity(
  name: Name,
  taxId: Option[TaxId]
)

object PartyIdentity {
  case class Name(value: String)  extends AnyVal
  case class TaxId(value: String) extends AnyVal
}
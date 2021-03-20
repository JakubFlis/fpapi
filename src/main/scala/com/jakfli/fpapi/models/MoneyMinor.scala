package com.jakfli.fpapi.models

import com.jakfli.fpapi.models.MoneyMinor._
import enumeratum.EnumEntry.UpperSnakecase

case class MoneyMinor(
  amount: Amount,
  currency: Currency
) {
  def majorAmount(): Amount =
    Amount(amount.value * 100)
}

object MoneyMinor {
  import enumeratum._

  case class Amount(value: BigDecimal) extends AnyVal

  sealed trait Currency extends EnumEntry with UpperSnakecase
  object Currency extends Enum[Currency] {
    case object USD extends Currency
    case object EUR extends Currency
    val values: IndexedSeq[Currency] = findValues
  }
}
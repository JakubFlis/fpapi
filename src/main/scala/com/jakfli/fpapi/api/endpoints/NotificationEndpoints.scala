package com.jakfli.fpapi.api.endpoints

import com.jakfli.fpapi.api.responses.CreateOrderResponse
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.codec.enumeratum.TapirCodecEnumeratum
import sttp.tapir.generic.SchemaDerivation
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._

class NotificationEndpoints extends TapirCodecEnumeratum with SchemaDerivation {
  val notificationEndpoint: ZEndpoint[Unit, String, CreateOrderResponse] =
    endpoint
      .get
      .in("order")
      .errorOut(stringBody)
      .out(jsonBody[CreateOrderResponse])
      .out(statusCode(StatusCode.Ok))
}

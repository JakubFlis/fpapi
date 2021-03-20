package com.jakfli.fpapi.api.endpoints

import com.jakfli.fpapi.api.requests.CreateOrderRequest
import com.jakfli.fpapi.api.responses.CreateOrderResponse
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.codec.enumeratum.TapirCodecEnumeratum
import sttp.tapir.generic.SchemaDerivation
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._

class OrderEndpoints extends TapirCodecEnumeratum with SchemaDerivation {
  val placeOrderEndpoint: ZEndpoint[CreateOrderRequest, String, CreateOrderResponse] =
    endpoint
      .post
      .in("order")
      .errorOut(stringBody)
      .in(jsonBody[CreateOrderRequest])
      .out(jsonBody[CreateOrderResponse])
      .out(statusCode(StatusCode.Ok))
}

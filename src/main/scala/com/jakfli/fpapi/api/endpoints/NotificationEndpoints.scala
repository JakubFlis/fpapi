package com.jakfli.fpapi.api.endpoints

import com.jakfli.fpapi.api.requests.SendNotificationRequest
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.codec.enumeratum.TapirCodecEnumeratum
import sttp.tapir.generic.SchemaDerivation
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._

class NotificationEndpoints extends TapirCodecEnumeratum with SchemaDerivation {
  private val notificationBase =
    endpoint
      .tag("Notifications")

  val sendNotificationEndpoint: ZEndpoint[SendNotificationRequest, String, Unit] =
    notificationBase
      .get
      .in("notification")
      .in(jsonBody[SendNotificationRequest])
      .errorOut(stringBody)
      .out(statusCode(StatusCode.Ok))
}

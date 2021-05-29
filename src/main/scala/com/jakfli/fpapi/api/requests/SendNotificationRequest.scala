package com.jakfli.fpapi.api.requests

import com.jakfli.fpapi.api.requests.SendNotificationRequest.{RequestNotificationMessage, RequestNotificationTemplate}
import com.jakfli.fpapi.models.Address.EmailAddress

case class SendNotificationRequest(
  emailAddress: EmailAddress,
  template: RequestNotificationTemplate,
  message: RequestNotificationMessage
)

object SendNotificationRequest {
  case class RequestNotificationTemplate(value: String) extends AnyVal
  case class RequestNotificationMessage(value: String) extends AnyVal
}
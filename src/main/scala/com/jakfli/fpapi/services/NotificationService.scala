package com.jakfli.fpapi.services

import com.jakfli.fpapi.models.Address.EmailAddress
import com.jakfli.fpapi.services.NotificationService.NotificationMessage
import zio.logging.Logger
import zio.{Has, RIO, Task, URLayer, ZIO, ZLayer}

class NotificationService(log: Logger[String]) extends NotificationService.Service {
  override def sendNotification(emailAddress: EmailAddress, template: NotificationService.NotificationTemplate, message: NotificationMessage): Task[Unit] =
    for {
      _ <- log.info(s"Sending mail of template [${template.toString}] and message [${message.value}] to [${emailAddress.value}]...")
      _ <- log.info(s"Sending completed.")
    } yield ()
}

object NotificationService {
  type NotificationServiceM   = Has[Service]
  type NotificationServiceEnv = Has[Logger[String]]

  trait Service {
    def sendNotification(emailAddress: EmailAddress, template: NotificationTemplate, message: NotificationMessage): Task[Unit]
  }

  def sendNotification(emailAddress: EmailAddress, template: NotificationTemplate, message: NotificationMessage): RIO[NotificationServiceM, Unit] =
    ZIO.accessM(_.get.sendNotification(emailAddress, template, message))

  val live: URLayer[NotificationServiceEnv, NotificationServiceM] =
    (for {
      logger <- ZIO.service[Logger[String]]
    } yield new NotificationService(logger))
      .toLayer

  sealed trait NotificationTemplate
  case object OrderPlaced extends NotificationTemplate {
    override def toString: String =
      "order-placed-template"
  }
  case object OrderReadyToPickUp extends NotificationTemplate {
    override def toString: String =
      "order-ready-to-pick-up-template"
  }
  case object OrderCancelled extends NotificationTemplate {
    override def toString: String =
      "order-cancelled-template"
  }
  case object OrderDelivered extends NotificationTemplate {
    override def toString: String =
      "order-delivered-template"
  }
  object NotificationTemplate {
    def convertToTemplate(stringTemplate: String): Option[NotificationTemplate] = stringTemplate match {
      case "order-placed-template"            => Some(OrderPlaced)
      case "order-ready-to-pick-up-template"  => Some(OrderReadyToPickUp)
      case "order-cancelled-template"         => Some(OrderCancelled)
      case "order-delivered-template"         => Some(OrderDelivered)
      case _                                  => None
    }
  }

  case class NotificationMessage(value: String) extends AnyVal

  implicit class NotificationStringOps(message: String) {
    def asNotificationMessage: NotificationMessage =
      NotificationMessage(message)
  }
}

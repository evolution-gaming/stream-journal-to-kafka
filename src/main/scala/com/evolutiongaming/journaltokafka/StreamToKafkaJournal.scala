package com.evolutiongaming.journaltokafka

import akka.persistence.AtomicWrite
import akka.persistence.journal.AsyncWriteJournal

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try

trait StreamToKafkaJournal extends AsyncWriteJournal {
  import context.dispatcher

  private lazy val streamToKafka = newStreamToKafka()

  def newStreamToKafka(): Option[StreamToKafka]

  abstract override def asyncWriteMessages(messages: Seq[AtomicWrite]): Future[Seq[Try[Unit]]] = {
    val result = super.asyncWriteMessages(messages)

    val resultWithStream = for {
      streamToKafka <- streamToKafka
      message <- messages.headOption
    } yield for {
      result <- result
    } yield {
      if (result forall { _.isSuccess }) {
        val persistenceId = message.persistenceId
        streamToKafka(message.persistenceId, messages).failed foreach { failure =>
          def events = messages.flatMap { _.payload } mkString ", "

          context.system.log.error(s"$persistenceId streamToKafka failed for $events: $failure", failure)
        }
      }
      result
    }

    resultWithStream getOrElse result
  }
}
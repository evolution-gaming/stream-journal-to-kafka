package com.evolutiongaming.journaltokafka

import akka.persistence.{AtomicWrite, PersistentRepr}
import com.evolutiongaming.skafka.producer.Producer
import com.evolutiongaming.skafka.producer.Producer.Record

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

trait StreamToKafka {
  import StreamToKafka._

  def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]): Future[Unit]
}

object StreamToKafka {

  type PersistenceId = String

  lazy val Empty: StreamToKafka = {
    val futureUnit = Future.successful(())
    new StreamToKafka {
      def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]) = futureUnit
    }
  }


  def apply(
    producer: Producer[String, PersistentRepr],
    topic: PersistenceId => Option[String])(
    implicit ec: ExecutionContext): StreamToKafka = {

    new StreamToKafka {
      def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]): Future[Unit] = {

        val result = for {
          topic <- topic(persistenceId).toSeq
          atomicWrite <- messages
          persistentRepr <- atomicWrite.payload
        } yield {
          val record = Record(topic = topic, value = persistentRepr, key = Some(persistenceId))
          producer.send(record)
        }
        Future.sequence(result) map { _ => () }
      }
    }
  }
}
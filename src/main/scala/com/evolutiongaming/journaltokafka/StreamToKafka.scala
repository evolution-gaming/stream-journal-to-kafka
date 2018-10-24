package com.evolutiongaming.journaltokafka

import akka.actor.ActorSystem
import akka.persistence.{AtomicWrite, PersistentRepr}
import akka.serialization.SerializationExtension
import com.evolutiongaming.concurrent.FutureHelper._
import com.evolutiongaming.skafka.producer.{Producer, ProducerRecord}
import com.evolutiongaming.skafka.{ToBytes, Topic}

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.control.NonFatal

trait StreamToKafka {
  import StreamToKafka._

  def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]): Future[Unit]
}

object StreamToKafka {

  type PersistenceId = String

  lazy val Empty: StreamToKafka = new StreamToKafka {
    def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]) = Future.unit
  }

  def apply(producer: Producer.Send[Future], topic: PersistenceId => Option[String])
    (implicit toBytes: ToBytes[PersistentRepr]): StreamToKafka = {

    new StreamToKafka {
      def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]): Future[Unit] = {

        val result = for {
          topic <- topic(persistenceId).toSeq
          atomicWrite <- messages
          persistentRepr <- atomicWrite.payload
        } yield {
          val record = ProducerRecord(topic = topic, value = persistentRepr, key = persistenceId)
          producer(record)
        }
        Future.foldUnit(result)
      }
    }
  }

  def apply(producer: Producer.Send[Future], topic: PersistenceId => Option[String], system: ActorSystem): StreamToKafka = {
    val serialization = SerializationExtension(system)
    val toBytes = new ToBytes[PersistentRepr] {
      def apply(value: PersistentRepr, topic: Topic) = {
        try serialization.serialize(value).get catch {
          case NonFatal(failure) => throw new RuntimeException(s"Failed to serialize $value", failure)
        }
      }
    }
    apply(producer, topic)(toBytes)
  }
}
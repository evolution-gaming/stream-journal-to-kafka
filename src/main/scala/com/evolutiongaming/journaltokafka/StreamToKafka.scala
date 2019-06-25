package com.evolutiongaming.journaltokafka

import akka.actor.ActorSystem
import akka.persistence.{AtomicWrite, PersistentRepr}
import akka.serialization.SerializationExtension
import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad, ~>}
import com.evolutiongaming.skafka.producer.{Producer, ProducerRecord, RecordMetadata}
import com.evolutiongaming.skafka.{ToBytes, Topic}

import scala.collection.immutable.Seq
import scala.util.control.NonFatal

trait StreamToKafka[F[_]] {
  import StreamToKafka._

  def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]): F[Unit]
}

object StreamToKafka {

  type PersistenceId = String

  def empty[F[_] : Applicative]: StreamToKafka[F] = const(().pure[F])

  def const[F[_]](unit: F[Unit]): StreamToKafka[F] = new StreamToKafka[F] {
    def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]) = unit
  }


  def apply[F[_] : Monad](
    send: Producer.Send[F],
    topic: PersistenceId => F[Option[String]],
    toBytes: ToBytes[PersistentRepr],
  ): StreamToKafka[F] = {

    new StreamToKafka[F] {
      def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]) = {

        def sends(topic: Option[String]): List[F[F[RecordMetadata]]] = for {
          topic          <- topic.toList
          atomicWrite    <- messages
          persistentRepr <- atomicWrite.payload
          record          = ProducerRecord(topic = topic, value = persistentRepr, key = persistenceId)
        } yield {
          send(record)(implicitly[ToBytes[String]], toBytes)
        }

        def fold[A](fa: List[F[A]]): F[List[A]] = {
          for {
            as <- fa.foldLeftM(List.empty[A]) { (as, fa) => for {a <- fa} yield a :: as }
          } yield {
            as.reverse
          }
        }

        for {
          topic  <- topic(persistenceId)
          bocks  <- fold(sends(topic))
          _      <- fold(bocks)
        } yield {}
      }
    }
  }

  def of[F[_] : Sync](
    producer: Producer.Send[F],
    topic: PersistenceId => F[Option[String]],
    system: ActorSystem,
  ): F[StreamToKafka[F]] = {

    for {
      serialization <- Sync[F].delay { SerializationExtension(system) }
    } yield {
      val toBytes = new ToBytes[PersistentRepr] {
        def apply(value: PersistentRepr, topic: Topic) = {
          try serialization.serialize(value).get catch {
            case NonFatal(error) => throw new RuntimeException(s"Failed to serialize $value", error)
          }
        }
      }
      apply[F](producer, topic, toBytes)
    }
  }


  implicit class StreamToKafkaOps[F[_]](val self: StreamToKafka[F]) extends AnyVal {

    def mapK[G[_]](f: F ~> G): StreamToKafka[G] = new StreamToKafka[G] {
      def apply(persistenceId: PersistenceId, messages: Seq[AtomicWrite]) = f(self(persistenceId, messages))
    }
  }
}
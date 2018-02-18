package com.evolutiongaming.journaltokafka

import akka.persistence.{AtomicWrite, PersistentRepr}
import com.evolutiongaming.concurrent.CurrentThreadExecutionContext
import com.evolutiongaming.skafka.producer.{Producer, ToBytes}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Future
import scala.util.Success

class StreamToKafkaSpec extends FunSuite with Matchers {

  test("apply") {
    implicit val ec = CurrentThreadExecutionContext

    var records = List.empty[Producer.Record[String, PersistentRepr]]

    val producer = new Producer.Send {
      def doApply[K, V](record: Producer.Record[K, V])
        (implicit valueToBytes: ToBytes[V], keyToBytes: ToBytes[K]) = {
        val persistentRepr = record.asInstanceOf[Producer.Record[String, PersistentRepr]]
        records = persistentRepr :: records
        val metadata = Producer.RecordMetadata(record.topic, 0)
        Future.successful(metadata)
      }
    }

    implicit val valueToBytes = ToBytes.empty[PersistentRepr]

    val streamToKafka = StreamToKafka(producer, PartialFunction.condOpt(_) { case "id1" => "topic" })

    streamToKafka("id1", Nil).value shouldEqual Some(Success(()))
    records.size shouldEqual 0

    val persistentRepr = PersistentRepr("payload")
    val atomicWrite = AtomicWrite(persistentRepr)
    streamToKafka("id1", List(atomicWrite)).value shouldEqual Some(Success(()))
    records.size shouldEqual 1

    streamToKafka("id2", List(atomicWrite)).value shouldEqual Some(Success(()))
    records.size shouldEqual 1
  }
}

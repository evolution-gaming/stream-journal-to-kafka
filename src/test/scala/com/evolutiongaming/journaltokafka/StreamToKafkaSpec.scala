package com.evolutiongaming.journaltokafka

import akka.persistence.{AtomicWrite, PersistentRepr}
import com.evolutiongaming.concurrent.CurrentThreadExecutionContext
import com.evolutiongaming.skafka.producer.{Producer, ProducerRecord, RecordMetadata}
import com.evolutiongaming.skafka.{ToBytes, TopicPartition}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Future
import scala.util.Success

class StreamToKafkaSpec extends FunSuite with Matchers {

  test("apply") {
    implicit val ec = CurrentThreadExecutionContext

    var records = List.empty[ProducerRecord[String, PersistentRepr]]

    val producer = new Producer.Send {
      def doApply[K, V](record: ProducerRecord[K, V])
        (implicit valueToBytes: ToBytes[V], keyToBytes: ToBytes[K]) = {
        val persistentRepr = record.asInstanceOf[ProducerRecord[String, PersistentRepr]]
        records = persistentRepr :: records
        val topicPartition = TopicPartition(record.topic, 0)
        val metadata = RecordMetadata(topicPartition)
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

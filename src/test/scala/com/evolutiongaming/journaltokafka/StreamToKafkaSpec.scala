package com.evolutiongaming.journaltokafka

import akka.persistence.{AtomicWrite, PersistentRepr}
import cats.Id
import com.evolutiongaming.skafka.producer.{Producer, ProducerRecord, RecordMetadata}
import com.evolutiongaming.skafka.{ToBytes, TopicPartition}
import org.scalatest.{FunSuite, Matchers}

class StreamToKafkaSpec extends FunSuite with Matchers {

  test("apply") {
    var records = List.empty[ProducerRecord[String, PersistentRepr]]

    val producer = new Producer.Send[Id] {
      def apply[K, V](
        record: ProducerRecord[K, V])(implicit
        toBytesK: ToBytes[Id, K], toBytesV: ToBytes[Id, V]
      ) = {
        val persistentRepr = record.asInstanceOf[ProducerRecord[String, PersistentRepr]]
        records = persistentRepr :: records
        val topicPartition = TopicPartition(record.topic, 0)
        RecordMetadata(topicPartition)
      }
    }

    implicit val valueToBytes = ToBytes.empty[Id, PersistentRepr]

    val streamToKafka = StreamToKafka[Id](producer, PartialFunction.condOpt(_) { case "id1" => "topic" }, valueToBytes)

    streamToKafka("id1", Nil)
    records.size shouldEqual 0

    val persistentRepr = PersistentRepr("payload")
    val atomicWrite = AtomicWrite(persistentRepr)
    streamToKafka("id1", List(atomicWrite))
    records.size shouldEqual 1

    streamToKafka("id2", List(atomicWrite))
    records.size shouldEqual 1
  }
}

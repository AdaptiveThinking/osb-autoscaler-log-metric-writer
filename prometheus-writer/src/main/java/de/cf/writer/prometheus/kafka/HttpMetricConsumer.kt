package de.cf.writer.prometheus.kafka

import com.google.protobuf.InvalidProtocolBufferException

import de.cf.autoscaler.kafka.AutoScalerConsumer
import de.cf.autoscaler.kafka.ByteConsumerThread
import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.HttpMetric
import de.cf.autoscaler.kafka.protobuf.ProtobufHttpMetricWrapper.ProtoHttpMetric
import de.cf.writer.prometheus.PrometheusWriter

class HttpMetricConsumer(groupId: String, val kafkaPropertiesBean: KafkaPropertiesBean,
                         private val writer: PrometheusWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread = ByteConsumerThread(kafkaPropertiesBean.metricContainerTopic,
            groupId,
            kafkaPropertiesBean.host,
            kafkaPropertiesBean.port, this)

    override fun startConsumer() {
        consThread.start()
    }

    override fun stopConsumer() {
        consThread.kafkaConsumer.wakeup()
    }

    override fun consume(bytes: ByteArray) {
        try {
            val metric = HttpMetric(ProtoHttpMetric.parseFrom(bytes))
            writer.writeHttpMetric(metric)

        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

    }

    override fun getType(): String {
        return "metric_http"
    }

}

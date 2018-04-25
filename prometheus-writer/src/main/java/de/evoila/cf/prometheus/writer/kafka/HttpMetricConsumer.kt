package de.evoila.cf.prometheus.writer.kafka

import com.google.protobuf.InvalidProtocolBufferException

import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.HttpMetric
import de.evoila.cf.autoscaler.kafka.protobuf.PbHttpMetric.ProtoHttpMetric
import de.evoila.cf.prometheus.PrometheusWriter

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

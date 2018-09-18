package de.evoila.cf.prometheus.writer.kafka

import com.google.protobuf.InvalidProtocolBufferException

import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.ApplicationMetric
import de.evoila.cf.autoscaler.kafka.protobuf.PbApplicationMetric.ProtoApplicationMetric
import de.evoila.cf.prometheus.PrometheusWriter

class ApplicationMetricConsumer(val groupId: String, val kafkaPropertiesBean: KafkaPropertiesBean,
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
            val metric = ApplicationMetric(ProtoApplicationMetric.parseFrom(bytes))
            //writer.writeApplicationContainerMetric(metric)

        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

    }

    override fun getType(): String {
        return "metric_container_application"
    }

}

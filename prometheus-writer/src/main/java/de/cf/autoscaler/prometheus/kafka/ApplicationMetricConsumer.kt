package de.cf.autoscaler.prometheus.kafka


import com.google.protobuf.InvalidProtocolBufferException

import de.cf.autoscaler.kafka.AutoScalerConsumer
import de.cf.autoscaler.kafka.ByteConsumerThread
import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.ApplicationMetric
import de.cf.autoscaler.kafka.protobuf.ProtobufApplicationMetricWrapper.ProtoApplicationMetric
import de.cf.autoscaler.prometheus.prometheus.PrometheusWriter

class ApplicationMetricConsumer(val groupId: String, val kafkaPropertiesBean: KafkaPropertiesBean,
                                private val writer: PrometheusWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread

    init {
        consThread = ByteConsumerThread(kafkaPropertiesBean.metricContainerTopic,
                groupId,
                kafkaPropertiesBean.host,
                kafkaPropertiesBean.port, this)
    }

    override fun startConsumer() {
        consThread.start()
    }

    override fun stopConsumer() {
        consThread.kafkaConsumer.wakeup()
    }

    override fun consume(bytes: ByteArray) {
        try {
            val metric = ApplicationMetric(ProtoApplicationMetric.parseFrom(bytes))
            writer.writeApplicationContainerMetric(metric)

        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

    }

    override fun getType(): String {
        return "metric_container_application"
    }

}

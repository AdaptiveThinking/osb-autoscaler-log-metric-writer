package de.cf.writer.prometheus.kafka

import com.google.protobuf.InvalidProtocolBufferException

import de.cf.autoscaler.kafka.AutoScalerConsumer
import de.cf.autoscaler.kafka.ByteConsumerThread
import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.ContainerMetric
import de.cf.autoscaler.kafka.protobuf.ProtobufContainerMetricWrapper.ProtoContainerMetric
import de.cf.writer.prometheus.PrometheusWriter

class InstanceMetricConsumer(val groupId: String, val kafkaPropertiesBean: KafkaPropertiesBean,
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
            val metric = ContainerMetric(ProtoContainerMetric.parseFrom(bytes))
            writer.writeInstanceContainerMetric(metric)

        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

    }

    override fun getType(): String {
        return "metric_container_instance"
    }

}

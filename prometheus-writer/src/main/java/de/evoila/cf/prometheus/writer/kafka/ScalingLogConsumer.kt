package de.evoila.cf.prometheus.writer.kafka

import com.google.protobuf.InvalidProtocolBufferException

import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog
import de.evoila.cf.autoscaler.kafka.protobuf.PbScalingLog.ProtoScalingLog
import de.evoila.cf.prometheus.PrometheusWriter

class ScalingLogConsumer(groupId: String, kafkaPropertiesBean: KafkaPropertiesBean,
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
            val log = ScalingLog(ProtoScalingLog.parseFrom(bytes))
            writer.writeScalingLog(log)

        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }

    }

    override fun getType(): String {
        return "quotient"
    }

}

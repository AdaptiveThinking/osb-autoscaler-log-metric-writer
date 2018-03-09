package de.cf.writer.prometheus.kafka

import com.google.protobuf.InvalidProtocolBufferException

import de.cf.autoscaler.kafka.AutoScalerConsumer
import de.cf.autoscaler.kafka.ByteConsumerThread
import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.ScalingLog
import de.cf.autoscaler.kafka.protobuf.ProtobufScalingLogWrapper.ProtoScalingLog
import de.cf.writer.prometheus.PrometheusWriter

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

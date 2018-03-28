package de.evoila.cf.elasticsearch.writer.kafka

import com.google.protobuf.InvalidProtocolBufferException
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.LogMessage
import de.evoila.cf.autoscaler.kafka.protobuf.PbLogMessage.ProtoLogMessage
import de.evoila.cf.elasticsearch.ElasticsearchWriter

/**
 * Created by reneschollmeyer, evoila on 07.03.18.
 */
class LogMessageConsumer(val groupId: String, val kafkaPropertiesBean: KafkaPropertiesBean,
                         private val writer: ElasticsearchWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread = ByteConsumerThread(kafkaPropertiesBean.logMessageTopic,
            groupId,
            kafkaPropertiesBean.host,
            kafkaPropertiesBean.port,
            this)

    override fun startConsumer() {
        consThread.start()
    }

    override fun stopConsumer() {
        consThread.kafkaConsumer.wakeup()
    }

    override fun consume(bytes: ByteArray?) {
        try {
            val logMessage = LogMessage(ProtoLogMessage.parseFrom(bytes))
            writer.writeLogMessage(logMessage)
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }
    }

    override fun getType(): String {
        return "log_messages"
    }
}
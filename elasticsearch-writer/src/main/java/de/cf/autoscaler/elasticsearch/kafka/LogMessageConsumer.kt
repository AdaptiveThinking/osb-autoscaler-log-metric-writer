package de.cf.autoscaler.elasticsearch.kafka

import com.google.protobuf.InvalidProtocolBufferException
import de.cf.autoscaler.elasticsearch.ElasticsearchWriter
import de.cf.autoscaler.kafka.AutoScalerConsumer
import de.cf.autoscaler.kafka.ByteConsumerThread
import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.LogMessage
import de.cf.autoscaler.kafka.protobuf.ProtobufLogMessageWrapper.ProtoLogMessage

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
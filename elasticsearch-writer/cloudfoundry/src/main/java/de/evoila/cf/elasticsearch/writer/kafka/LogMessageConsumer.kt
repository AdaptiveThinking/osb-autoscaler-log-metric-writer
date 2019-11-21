package de.evoila.cf.elasticsearch.writer.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.LogMessage
import de.evoila.cf.elasticsearch.ElasticsearchWriter


/**
 * Created by reneschollmeyer, evoila on 07.03.18.
 */
class LogMessageConsumer(groupId: String, kafkaPropertiesBean: KafkaPropertiesBean,
                         private val writer: ElasticsearchWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread = ByteConsumerThread(kafkaPropertiesBean.logMessageTopic,
            groupId,
            this,
            kafkaPropertiesBean)

    override fun startConsumer() {
        consThread.start()
    }

    override fun stopConsumer() {
        consThread.kafkaConsumer.wakeup()
    }

    override fun consume(bytes: ByteArray?) {
        val logMessage = ObjectMapper().readValue(bytes, LogMessage::class.java)
        writer.writeLogMessage(logMessage, "logmessages")
    }

    override fun getType(): String {
        return "log_messages"
    }

    override fun removeConsumer(topicName: String?) {}
}
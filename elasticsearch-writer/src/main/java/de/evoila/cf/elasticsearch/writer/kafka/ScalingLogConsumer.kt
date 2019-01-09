package de.evoila.cf.elasticsearch.writer.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog
import de.evoila.cf.elasticsearch.ElasticsearchWriter

class ScalingLogConsumer(groupId: String, kafkaPropertiesBean: KafkaPropertiesBean,
                         private val writer: ElasticsearchWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread = ByteConsumerThread(kafkaPropertiesBean.scalingTopic,
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
        val scalingLog = ObjectMapper().readValue(bytes, ScalingLog::class.java)
        writer.writeScalingLog(scalingLog, "scalinglogs")
    }

    override fun getType(): String {
        return "quotient"
    }

}

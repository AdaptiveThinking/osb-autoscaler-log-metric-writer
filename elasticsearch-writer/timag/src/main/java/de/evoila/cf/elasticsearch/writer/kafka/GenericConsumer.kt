package de.evoila.cf.elasticsearch.writer.kafkay

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.elasticsearch.ElasticsearchWriterTimAG
import de.evoila.cf.elasticsearch.writer.redis.RedisClientConnector

/**
 * Created by reneschollmeyer, evoila on 12.07.19.
 */
class GenericConsumer(groupId: String, kafkaPropertiesBean: KafkaPropertiesBean,
                      private val writer: ElasticsearchWriterTimAG, private val topicName: String,
                      private val redisClientConnector: RedisClientConnector) : AutoScalerConsumer {

    private val redisTopicKey = "timag_consumer"
    private val objectMapper = ObjectMapper()
    private val consThread: ByteConsumerThread = ByteConsumerThread(topicName,
            groupId,
            this,
            kafkaPropertiesBean)

    override fun startConsumer() {
        consThread.start()
    }

    override fun stopConsumer() {
        consThread.kafkaConsumer.wakeup()
    }

    override fun consume(bytes: ByteArray) {
        writer.writeJson(bytes, topicName)
    }

    override fun getType(): String {
        return ""
    }

    override fun removeConsumer(topicName: String) {
        val topics = redisClientConnector[redisTopicKey]
        var topicList = listOf<String>()

        topics?.let{topics ->
            topicList = objectMapper.readValue(topics)
        }

        topicList = topicList.minus(topicName)

        topicList = topicList.map{k -> "\"$k\""}

        redisClientConnector[redisTopicKey] = topicList.toString()
    }
}
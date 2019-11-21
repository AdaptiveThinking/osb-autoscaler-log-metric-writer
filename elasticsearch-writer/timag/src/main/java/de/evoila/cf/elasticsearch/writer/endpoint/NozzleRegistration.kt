package de.evoila.cf.elasticsearch.writer.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.elasticsearch.ElasticsearchWriterTimAG
import de.evoila.cf.elasticsearch.writer.kafkay.GenericConsumer
import de.evoila.cf.elasticsearch.writer.model.Nozzle
import de.evoila.cf.elasticsearch.writer.redis.RedisClientConnector
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct





/**
 * Created by reneschollmeyer, evoila on 12.07.19.
 */
@RestController
@RequestMapping
@ConditionalOnProperty(prefix = "platform", name = ["name"], havingValue = "timag")
class NozzleRegistration @Autowired constructor(private val kafkaPropertiesBean: KafkaPropertiesBean,
                                                private val elasticsearchWriterTimAG: ElasticsearchWriterTimAG,
                                                private val redisClientConnector: RedisClientConnector){

    private val log = LoggerFactory.getLogger(NozzleRegistration::class.java)
    private val redisTopicKey = "timag_consumer"
    private val objectMapper = ObjectMapper()

    @PostConstruct
    fun loadConsumer() {
        val topicList = getTopicList()

        topicList.forEach { entry ->
            log.info("Found topic $entry. Creating consumer...")
            createConsumer(entry)
        }
    }

    @PutMapping(value = ["/register"])
    fun registerNozzle(@RequestBody nozzle: Nozzle): ResponseEntity<Nozzle>? {

        nozzle.endpoints?.forEach { endpoint ->
            createConsumer(nozzle.nozzleId + "-" + endpoint)
        }

        var topicList = getTopicList()

        nozzle.endpoints?.forEach {endpoint ->
            val topic = "${nozzle.nozzleId}-$endpoint"

            if(!topicList.contains(topic)) {
                topicList = topicList.plus(topic)
            }
        }

        topicList = topicList.map{k -> "\"$k\""}

        redisClientConnector[redisTopicKey] = topicList.toString()

        return ResponseEntity(nozzle, HttpStatus.OK)
    }

    private fun createConsumer(topicName: String) {
        log.info("Creating consumer for topic $topicName")
        val genericConsumer = GenericConsumer("writer_tim_data",
                kafkaPropertiesBean, elasticsearchWriterTimAG, topicName, redisClientConnector)
        genericConsumer.startConsumer()
    }

    private fun getTopicList(): List<String> {
        val topics = redisClientConnector[redisTopicKey]
        var topicList = listOf<String>()

        topics?.let{topics ->
            topicList = objectMapper.readValue(topics)
        }

        return topicList
    }
}
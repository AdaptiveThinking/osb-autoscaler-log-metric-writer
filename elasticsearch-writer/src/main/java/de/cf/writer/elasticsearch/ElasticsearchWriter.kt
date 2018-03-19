package de.cf.writer.elasticsearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.LogMessage
import de.cf.writer.elasticsearch.beans.ElasticsearchPropertiesBean
import de.cf.writer.elasticsearch.kafka.LogMessageConsumer
import de.cf.writer.elasticsearch.model.ElasticsearchWriterObject
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Date
import java.util.*
import javax.annotation.PostConstruct


/**
 * Created by reneschollmeyer, evoila on 07.03.18.
 */
@Component
class ElasticsearchWriter @Autowired constructor(
        private val kafkaPropertiesBean: KafkaPropertiesBean,
        private val elasticsearchPropertiesBean: ElasticsearchPropertiesBean
){

    val mapper: ObjectMapper = ObjectMapper()

    @Autowired
    private lateinit var elasticsearchRestClientFactory: ElasticsearchRestClientFactory

    @PostConstruct
    fun executeElasticSearchWriter() {
        logMessageConsumerRunner()
    }

    private fun logMessageConsumerRunner() {
        val logMessageConsumer = LogMessageConsumer("writer_log_messages",
                kafkaPropertiesBean, this)
        logMessageConsumer.startConsumer()
    }


    fun writeLogMessage(data: LogMessage) {

        System.out.println("LOG HIER: " + data.logMessage)

        var jsonString = mapper.writeValueAsString(ElasticsearchWriterObject(Date(data.timestamp),
                data.logMessage, data.logMessageType, data.appId))

        val entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)
        var client = elasticsearchRestClientFactory.getRestClientConnection()

        var endpoint = "/" + Date(data.timestamp).toString() + "/logMessages/" + Generators.randomBasedGenerator().generate()

        client.performRequest("PUT", endpoint, Collections.emptyMap(), entity)
    }
}
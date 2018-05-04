package de.evoila.cf.elasticsearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.LogMessage
import de.evoila.cf.elasticsearch.writer.kafka.LogMessageConsumer
import de.evoila.cf.elasticsearch.writer.model.ElasticsearchWriterObject
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
        private val kafkaPropertiesBean: KafkaPropertiesBean){

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

        var jsonString = mapper.writeValueAsString(ElasticsearchWriterObject(data.timestamp,
                data.logMessage, data.logMessageType, data.sourceType, data.appId, data.appName, data.space, data.organization))

        val entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)
        var client = elasticsearchRestClientFactory.getRestClientConnection()

        var endpoint = "/" + Date(data.timestamp).toString() + "/logMessages/" + Generators.randomBasedGenerator().generate()

        client.performRequest("PUT", endpoint, Collections.emptyMap(), entity)
    }
}
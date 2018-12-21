package de.evoila.cf.elasticsearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.LogMessage
import de.evoila.cf.elasticsearch.security.ElasticsearchPortAvailabilityVerifier
import de.evoila.cf.elasticsearch.writer.beans.ElasticsearchPropertiesBean
import de.evoila.cf.elasticsearch.writer.beans.GrokPatternBean
import de.evoila.cf.elasticsearch.writer.connection.ElasticsearchRestClientFactory
import de.evoila.cf.elasticsearch.writer.kafka.LogMessageConsumer
import de.evoila.cf.elasticsearch.writer.model.ElasticsearchWriterObject
import de.evoila.cf.elasticsearch.writer.model.GrokPatternMatcher
import groovy.json.JsonBuilder
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.Date
import javax.annotation.PostConstruct


/**
 * Created by reneschollmeyer, evoila on 07.03.18.
 */
@Component
class ElasticsearchWriter @Autowired constructor(
        private val kafkaPropertiesBean: KafkaPropertiesBean,
        grokPatternBean: GrokPatternBean,
        private val elasticsearchProperties: ElasticsearchPropertiesBean){

    private val CONNECTION_TIMEOUT = 10

    private val log = LoggerFactory.getLogger(ElasticsearchWriter::class.java!!)

    private val mapper = ObjectMapper()
    private val matcher = GrokPatternMatcher(grokPatternBean)
    private var writerObjectMap = hashMapOf<String, ElasticsearchWriterObject>()

    private lateinit var client: RestClient

    private lateinit var writerObject: ElasticsearchWriterObject

    @Autowired
    private lateinit var elasticsearchRestClientFactory: ElasticsearchRestClientFactory

    @Autowired
    private lateinit var availabilityVerifier: ElasticsearchPortAvailabilityVerifier

    @PostConstruct
    fun executeElasticSearchWriter() {
        client = elasticsearchRestClientFactory.getRestClientConnection()
        logMessageConsumerRunner()
    }

    private fun logMessageConsumerRunner() {
        val logMessageConsumer = LogMessageConsumer("writer_log_messages",
                kafkaPropertiesBean, this)
        logMessageConsumer.startConsumer()
    }

    fun writeLogMessage(data: LogMessage) {
        if(!data.logMessage.startsWith("\t")) {
            var jsonString = mapper.writeValueAsString(writerObjectMap[data.appId].let { it })
            writerObjectMap.remove(data.appId)

            try {
                if(jsonString != "null") {
                    val entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)

                    var endpoint = "/" + Date(data.timestamp).toString() + "/logMessages/" + Generators.randomBasedGenerator().generate()

                    var logMessageRequest = Request("PUT", endpoint)
                    logMessageRequest.apply {
                        this.entity = entity
                    }

                    client.performRequest(logMessageRequest)
                }
            } catch(ex: Exception) {
                log.error(ex.message)

                for (i in 0 until CONNECTION_TIMEOUT) {
                    log.info("Trying to reconnect Elasticsearch, attempt: " + (i+1))

                    var available = availabilityVerifier
                            .verifyServiceAvailability(elasticsearchProperties.host, elasticsearchProperties.port, true)

                    if(available) {
                        client = elasticsearchRestClientFactory.getRestClientConnection()
                        log.info("Connection to Elasticsearch established.")

                        break
                    }
                }
            }

            val matchMap = if(data.sourceType == "RTR") {
                matcher.match(data.logMessage)
            } else {
                null
            }

            writerObject = if(matchMap != null && !matchMap.isEmpty()) {
                ElasticsearchWriterObject(data.timestamp, System.currentTimeMillis() - data.timestamp, JsonBuilder(matchMap).toPrettyString().replace("\\", ""), data.logMessageType, data.sourceType,
                        data.appId, data.appName, data.space, data.organization, data.organization_guid, data.sourceInstance)
            } else {
                ElasticsearchWriterObject(data.timestamp, System.currentTimeMillis() - data.timestamp, data.logMessage, data.logMessageType, data.sourceType,
                        data.appId, data.appName, data.space, data.organization, data.organization_guid, data.sourceInstance)
            }

            writerObjectMap[writerObject.appId] = writerObject

        } else {
            if(writerObjectMap.containsKey(data.appId)) {
                var tmpWriterObject = writerObjectMap.getValue(data.appId)
                tmpWriterObject.logMessage += "\n" + data.logMessage
                writerObjectMap[data.appId] = tmpWriterObject
            }
        }
    }
}
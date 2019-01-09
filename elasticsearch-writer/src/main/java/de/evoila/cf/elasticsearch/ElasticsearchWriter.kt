package de.evoila.cf.elasticsearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.AutoscalerMetric
import de.evoila.cf.autoscaler.kafka.messages.LogMessage
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog
import de.evoila.cf.elasticsearch.security.ElasticsearchPortAvailabilityVerifier
import de.evoila.cf.elasticsearch.writer.beans.ElasticsearchPropertiesBean
import de.evoila.cf.elasticsearch.writer.beans.GrokPatternBean
import de.evoila.cf.elasticsearch.writer.connection.ElasticsearchRestClientFactory
import de.evoila.cf.elasticsearch.writer.kafka.ContainerMetricConsumer
import de.evoila.cf.elasticsearch.writer.kafka.HttpMetricConsumer
import de.evoila.cf.elasticsearch.writer.kafka.LogMessageConsumer
import de.evoila.cf.elasticsearch.writer.kafka.ScalingLogConsumer
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
import java.util.concurrent.Semaphore
import javax.annotation.PostConstruct


/**
 * Created by reneschollmeyer, evoila on 07.03.18.
 */
@Component
class ElasticsearchWriter @Autowired constructor(
        private val kafkaPropertiesBean: KafkaPropertiesBean,
        grokPatternBean: GrokPatternBean,
        private val elasticsearchProperties: ElasticsearchPropertiesBean,
        private val elasticsearchRestClientFactory: ElasticsearchRestClientFactory,
        private val availabilityVerifier: ElasticsearchPortAvailabilityVerifier){

    private val mapper = ObjectMapper()
    private val matcher = GrokPatternMatcher(grokPatternBean)
    private var writerObjectMap = hashMapOf<String, LogMessage>()

    private val CONNECTION_TIMEOUT = 10
    private val log = LoggerFactory.getLogger(ElasticsearchWriter::class.java)

    private lateinit var client: RestClient

    private lateinit var writerObject: LogMessage

    private val available = Semaphore(1, true)

    @PostConstruct
    fun executeElasticSearchWriter() {
        client = elasticsearchRestClientFactory.getRestClientConnection()
        logMessageConsumerRunner()
    }

    private fun logMessageConsumerRunner() {
        val logMessageConsumer = LogMessageConsumer("writer_log_messages",
                kafkaPropertiesBean, this)
        logMessageConsumer.startConsumer()

        val scalingLogConsumer = ScalingLogConsumer("writer_scaling_logs",
                kafkaPropertiesBean, this)
        scalingLogConsumer.startConsumer()

        val containerMetricConsumer = ContainerMetricConsumer("writer_container_metrics",
                kafkaPropertiesBean, this)
        containerMetricConsumer.startConsumer()

        val httpMetricConsumer = HttpMetricConsumer("writer_http_metrics",
                kafkaPropertiesBean, this)
        httpMetricConsumer.startConsumer()
    }

    fun writeLogMessage(data: LogMessage, indexName: String) {
        if(!data.logMessage.startsWith("\t")) {
            var jsonString = mapper.writeValueAsString(writerObjectMap[data.appId].let { it })
            writerObjectMap.remove(data.appId)

            if(jsonString != "null") {
                val entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)

                var endpoint = "/${Date(data.timestamp)}-$indexName/_doc/${Generators.randomBasedGenerator().generate()}"

                performRequest("PUT", endpoint, entity)
            }

            val matchMap = if(data.sourceType == "RTR") {
                matcher.match(data.logMessage)
            } else {
                null
            }

            writerObject = if(matchMap != null && !matchMap.isEmpty()) {
                LogMessage(data.timestamp, JsonBuilder(matchMap).toPrettyString().replace("\\", ""), data.logMessageType, data.sourceType,
                        data.appId, data.appName, data.space, data.organization, data.organizationGuid, data.sourceInstance)
            } else {
                data
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

    fun writeScalingLog(data: ScalingLog, indexName: String) {
        var jsonString = mapper.writeValueAsString(data)

        val entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)

        var endpoint = "/${Date(data.timestamp)}-$indexName/_doc/${Generators.randomBasedGenerator().generate()}"

        performRequest("PUT", endpoint, entity)
    }

    fun writeMetric(data: AutoscalerMetric, indexName: String) {
        var jsonString = mapper.writeValueAsString(data)

        val entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)

        var endpoint = "/${Date(data.timestamp)}-$indexName/_doc/${Generators.randomBasedGenerator().generate()}"

        performRequest("PUT", endpoint, entity)
    }

    private fun performRequest(httpMethod: String, endpoint: String, entity: NStringEntity) {
        var request = Request(httpMethod, endpoint)
        request.apply {
            this.entity = entity
        }

        try {
            client.performRequest(request)
        } catch (ex: Exception) {
            available.acquire()
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

            available.release()
        }
    }
}
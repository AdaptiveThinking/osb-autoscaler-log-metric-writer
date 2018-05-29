package de.evoila.cf.elasticsearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.LogMessage
import de.evoila.cf.elasticsearch.writer.beans.GrokPatternBean
import de.evoila.cf.elasticsearch.writer.connection.ElasticsearchRestClientFactory
import de.evoila.cf.elasticsearch.writer.kafka.LogMessageConsumer
import de.evoila.cf.elasticsearch.writer.model.ElasticsearchWriterObject
import de.evoila.cf.elasticsearch.writer.model.GrokPatternMatcher
import groovy.json.JsonBuilder
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
        grokPatternBean: GrokPatternBean){

    private val mapper = ObjectMapper()
    private val matcher = GrokPatternMatcher(grokPatternBean)
    private var writerObjectMap = hashMapOf<String, ElasticsearchWriterObject>()

    private lateinit var writerObject: ElasticsearchWriterObject

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
        if(!data.logMessage.startsWith("\t")) {
            var jsonString = mapper.writeValueAsString(writerObjectMap[data.appId].let { it })
            writerObjectMap.remove(data.appId)

            if(jsonString != "null") {
                val entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)

                var client = elasticsearchRestClientFactory.getRestClientConnection()

                var endpoint = "/" + Date(data.timestamp).toString() + "/logMessages/" + Generators.randomBasedGenerator().generate()

                client.performRequest("PUT", endpoint, Collections.emptyMap(), entity)
            }

            val matchMap = if(data.sourceType == "RTR") {
                matcher.match(data.logMessage)
            } else {
                null
            }

            writerObject = if(matchMap != null && !matchMap.isEmpty()) {
                ElasticsearchWriterObject(data.timestamp, JsonBuilder(matchMap).toPrettyString().replace("\\", ""), data.logMessageType, data.sourceType,
                        data.appId, data.appName, data.space, data.organization)
            } else {
                ElasticsearchWriterObject(data.timestamp, data.logMessage, data.logMessageType, data.sourceType,
                        data.appId, data.appName, data.space, data.organization)
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
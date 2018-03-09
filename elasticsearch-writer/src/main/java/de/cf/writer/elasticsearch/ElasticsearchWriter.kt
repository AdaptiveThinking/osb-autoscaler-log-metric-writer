package de.cf.writer.elasticsearch

import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.LogMessage
import de.cf.writer.elasticsearch.beans.ElasticsearchPropertiesBean
import de.cf.writer.elasticsearch.kafka.LogMessageConsumer
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.sql.Date
import javax.annotation.PostConstruct


/**
 * Created by reneschollmeyer, evoila on 07.03.18.
 */
@Component
class ElasticsearchWriter @Autowired constructor(
        private val kafkaPropertiesBean: KafkaPropertiesBean,
        private val elasticsearchPropertiesBean: ElasticsearchPropertiesBean
){

    private lateinit var connection: TransportClient

    @PostConstruct
    fun executeElasticSearchWriter() {
        logMessageConsumerRunner()
    }

    private fun logMessageConsumerRunner() {
        val logMessageConsumer = LogMessageConsumer("writer_log_messages",
                kafkaPropertiesBean, this)
        logMessageConsumer.startConsumer()
    }

    private fun connect(): TransportClient {
        var settings: Settings = Settings.builder()
                .put("cluster.name", elasticsearchPropertiesBean.clusterName)
                .build()

        connection = PreBuiltTransportClient(settings).addTransportAddress(
                TransportAddress(InetAddress.getByName(elasticsearchPropertiesBean.host),
                        elasticsearchPropertiesBean.port))

        return connection
    }

    fun writeLogMessage(data: LogMessage) {
        val response = connect()
                .prepareIndex(Date(data.timestamp).toString(),
                        data.logMessageType,
                        data.appId)
                .setSource(jsonBuilder()
                        .startObject()
                        .field("message", data.logMessage)
                        .endObject())
                .get()
    }
}
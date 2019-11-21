package de.evoila.cf.elasticsearch

import com.fasterxml.uuid.Generators
import de.evoila.cf.elasticsearch.security.ElasticsearchPortAvailabilityVerifier
import de.evoila.cf.elasticsearch.writer.beans.ElasticsearchPropertiesBean
import de.evoila.cf.elasticsearch.writer.connection.ElasticsearchRestClientFactory
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.Semaphore
import javax.annotation.PostConstruct


/**
 * Created by reneschollmeyer, evoila on 07.03.18.
 */
@Component
@ConditionalOnProperty(prefix = "platform", name = ["name"], havingValue = "timag")
class ElasticsearchWriterTimAG @Autowired constructor(
        private val elasticsearchProperties: ElasticsearchPropertiesBean,
        private val elasticsearchRestClientFactory: ElasticsearchRestClientFactory,
        private val availabilityVerifier: ElasticsearchPortAvailabilityVerifier){

    private val CONNECTION_TIMEOUT = 10
    private val log = LoggerFactory.getLogger(ElasticsearchWriterTimAG::class.java)

    private lateinit var client: RestClient

    private val available = Semaphore(1, true)

    @PostConstruct
    fun executeElasticSearchWriter() {
        client = elasticsearchRestClientFactory.getRestClientConnection()
    }

    fun writeJson(bytes: ByteArray, indexName: String) {
        var jsonString = String(bytes)

        val entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)

        var endpoint = "/$indexName/_doc/${Generators.randomBasedGenerator().generate()}"

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
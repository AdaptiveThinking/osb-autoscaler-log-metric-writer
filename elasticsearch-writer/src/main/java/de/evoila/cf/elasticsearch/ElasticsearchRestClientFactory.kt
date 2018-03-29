package de.evoila.cf.elasticsearch

import de.evoila.cf.elasticsearch.writer.beans.ElasticsearchPropertiesBean
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ElasticsearchRestClientFactory {

    var client: RestClient? = null

    @Autowired
    lateinit var properties: ElasticsearchPropertiesBean

    fun getRestClientConnection(): RestClient {
        client?.let { return it }


        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY,
                UsernamePasswordCredentials(properties.username, properties.password))
        val builder: RestClientBuilder = RestClient.builder(HttpHost(properties.host, properties.port, properties.scheme))
                .setHttpClientConfigCallback(object: RestClientBuilder.HttpClientConfigCallback {

                    override fun customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder {
                        val clientbuilder: HttpAsyncClientBuilder =  httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                        return clientbuilder

                    }

                })

        this.client = builder.build()
        return this.client!!

    }
}
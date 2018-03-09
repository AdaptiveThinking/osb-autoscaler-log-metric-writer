package de.cf.writer.configuration


import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.writer.elasticsearch.beans.ElasticsearchPropertiesBean
import de.cf.writer.prometheus.beans.PrometheusPropertiesBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@EnableConfigurationProperties(PrometheusPropertiesBean::class, KafkaPropertiesBean::class, ElasticsearchPropertiesBean::class)
@PropertySource("classpath:/application.yml")
open class WriterConfiguration {
}

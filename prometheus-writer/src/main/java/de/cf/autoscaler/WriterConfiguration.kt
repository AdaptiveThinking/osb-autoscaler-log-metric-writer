package de.cf.autoscaler

import de.cf.autoscaler.elasticsearch.beans.ElasticsearchPropertiesBean
import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.prometheus.beans.PrometheusPropertiesBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@EnableConfigurationProperties(PrometheusPropertiesBean::class, KafkaPropertiesBean::class, ElasticsearchPropertiesBean::class)
@PropertySource("classpath:/application.yml")
open class WriterConfiguration {
}

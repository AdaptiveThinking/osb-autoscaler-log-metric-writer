package de.evoila.cf.writer.configuration

import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.elasticsearch.writer.beans.ElasticsearchPropertiesBean
import de.evoila.cf.elasticsearch.writer.beans.GrokPatternBean
import de.evoila.cf.prometheus.writer.beans.PrometheusPropertiesBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(PrometheusPropertiesBean::class, KafkaPropertiesBean::class, ElasticsearchPropertiesBean::class, GrokPatternBean::class)
open class WriterConfiguration {
}

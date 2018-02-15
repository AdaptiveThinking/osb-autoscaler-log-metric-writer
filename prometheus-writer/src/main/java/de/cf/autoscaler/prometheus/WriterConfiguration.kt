package de.cf.autoscaler.prometheus

import de.cf.autoscaler.kafka.KafkaPropertiesBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

import de.cf.autoscaler.prometheus.beans.PrometheusPropertiesBean
import org.springframework.boot.context.properties.EnableConfigurationProperties

@Configuration
@EnableConfigurationProperties(PrometheusPropertiesBean::class, KafkaPropertiesBean::class)
@PropertySource("classpath:/application.yml")
open class WriterConfiguration {
}

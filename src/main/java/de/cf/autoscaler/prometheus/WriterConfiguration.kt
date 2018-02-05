package de.cf.autoscaler.prometheus

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

import de.cf.autoscaler.prometheus.beans.KafkaPropertiesBean
import de.cf.autoscaler.prometheus.beans.PrometheusPropertiesBean


@Configuration
@PropertySource("classpath:/application.yaml")
class WriterConfiguration {

    @Bean(name = arrayOf("kafkaProperties"))
    fun kafkaPropertiesBean(): KafkaPropertiesBean {
        return KafkaPropertiesBean()
    }

    @Bean(name = arrayOf("prometheusPropertiesBean"))
    fun prometheusPropertiesBean(): PrometheusPropertiesBean {
        return PrometheusPropertiesBean()
    }
}

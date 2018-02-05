package de.cf.autoscaler.prometheus.prometheus

import de.cf.autoscaler.prometheus.beans.PrometheusPropertiesBean
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.PushGateway
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import javax.annotation.PostConstruct



class PrometheusWriter {

    constructor(kafkaProperties: KafkaProperties, prometheusPropertiesBean: PrometheusPropertiesBean) {}

    @PostConstruct
    fun executePrometheusWriter() {

    }

    @Throws(Exception::class)
    fun executeBatchJob() {
        val registry = CollectorRegistry()
        val duration = Gauge.build()
                .name("my_batch_job_duration_seconds").help("Duration of my batch job in seconds.").register(registry)
        val durationTimer = duration.startTimer()
        try {
            // Your code here.

            // This is only added to the registry after success,
            // so that a previous success in the Pushgateway isn't overwritten on failure.
            val lastSuccess = Gauge.build()
                    .name("my_batch_job_last_success").help("Last time my batch job succeeded, in unixtime.").register(registry)
            lastSuccess.setToCurrentTime()
        } finally {
            durationTimer.setDuration()
            val pg = PushGateway("127.0.0.1:9091")
            pg.pushAdd(registry, "my_batch_job")
        }
    }
}
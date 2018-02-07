package de.cf.autoscaler.prometheus.prometheus

import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.ApplicationMetric
import de.cf.autoscaler.kafka.messages.ScalingLog
import de.cf.autoscaler.prometheus.beans.PrometheusPropertiesBean
import de.cf.autoscaler.prometheus.kafka.ApplicationMetricConsumer
import de.cf.autoscaler.prometheus.kafka.InstanceMetricConsumer
import de.cf.autoscaler.prometheus.kafka.ScalingLogConsumer
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.PushGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class PrometheusWriter @Autowired constructor(
    private val kafkaPropertiesBean: KafkaPropertiesBean,
    private val prometheusPropertiesBean: PrometheusPropertiesBean) {

    @PostConstruct
    fun executePrometheusWriter() {
        containerMetricConsumerRunner()
        applicationMetricConsumerRunner()
        scalingLogConsumerRunner()
        val pushGateway = PushGateway(prometheusPropertiesBean.host + ":" + prometheusPropertiesBean.port)

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

    fun containerMetricConsumerRunner() {
        val containerMetricConsumer: MutableList<InstanceMetricConsumer> = mutableListOf()
        for (i in 1 until kafkaPropertiesBean.containerConsumerCount) {
            containerMetricConsumer.add(InstanceMetricConsumer("writer_container_metric",
                kafkaPropertiesBean, this))

            containerMetricConsumer[i].startConsumer()
        }
    }

    fun applicationMetricConsumerRunner() {
        val applicationContainerMetricConsumer = ApplicationMetricConsumer("writer_applicaton_container_metric",
                kafkaPropertiesBean, this)
        applicationContainerMetricConsumer.startConsumer()
    }

    fun scalingLogConsumerRunner() {
        val scalingConsumer = ScalingLogConsumer("writer_scaling",
                kafkaPropertiesBean, this)
        scalingConsumer.startConsumer();
    }

    fun writeApplicationContainerMetric(data: ApplicationMetric) {

    }

    fun writeScalingLog(data: ScalingLog) {
        if (data != null) {
            val measurement = prometheusPropertiesBean.scalingMeasurement
            val component: String
            when (data.component) {
                ScalingLog.UNDEFINED_BASED -> component = "undefined"
                ScalingLog.HTTP_REQUEST_BASED -> component = "requests"
                ScalingLog.HTTP_LATENCY_BASED -> component = "latency"
                ScalingLog.CONTAINER_CPU_BASED -> component = "cpu"
                ScalingLog.CONTAINER_RAM_BASED -> component = "ram"
                ScalingLog.PREDICTOR_BASED -> component = "predictor"
                ScalingLog.LIMIT_BASED -> component = "limit"
            }
        }
    }


}
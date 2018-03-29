package de.evoila.cf.prometheus

import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.ApplicationMetric
import de.evoila.cf.autoscaler.kafka.messages.ContainerMetric
import de.evoila.cf.autoscaler.kafka.messages.HttpMetric
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog
import de.evoila.cf.prometheus.writer.beans.PrometheusPropertiesBean
import de.evoila.cf.prometheus.writer.constants.ApplicationMetricFields
import de.evoila.cf.prometheus.writer.constants.HttpMetricFields
import de.evoila.cf.prometheus.writer.constants.InstanceMetricFields
import de.evoila.cf.prometheus.writer.constants.ScalingFields
import de.evoila.cf.prometheus.writer.kafka.ApplicationMetricConsumer
import de.evoila.cf.prometheus.writer.kafka.InstanceMetricConsumer
import de.evoila.cf.prometheus.writer.kafka.ScalingLogConsumer
import de.evoila.cf.prometheus.writers.kafka.HttpMetricConsumer
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

    private val httpRegistry = CollectorRegistry()
    private val instanceContainerRegistry = CollectorRegistry()
    private val containerMetricRegistry = CollectorRegistry()
    private val applicationRegistry = CollectorRegistry()
    private lateinit var pushGateway: PushGateway

    /**
     * Http Metrics
     */
    private val httpRequestsGauge = httpMetricsGauge(HttpMetricFields.REQUESTS)

    private val httpLatencyGauge = httpMetricsGauge(HttpMetricFields.LATENCY)

    private fun httpMetricsGauge(httpMetricFields: HttpMetricFields): Gauge {
        return Gauge.build(httpMetricFields.httpMetric, DESCRIPTION)
                .labelNames(APP_ID_LABEL_NAME)
                .register(httpRegistry)
    }

    /**
     * Instance Container Metrics
     */
    private val cpuInstanceGauge = instanceContainerMetricsGauge(InstanceMetricFields.CPU)

    private val ramInstanceGauge = instanceContainerMetricsGauge(InstanceMetricFields.RAM)

    private fun instanceContainerMetricsGauge(instanceMetricFields: InstanceMetricFields): Gauge {
        return Gauge.build(instanceMetricFields.instanceMetric, DESCRIPTION)
                .labelNames(APP_INSTANCE, APP_ID_LABEL_NAME)
                .register(instanceContainerRegistry)
    }
    /**
     * Application Container Metrics
     */
    private val cpuGauge = applicationContainerMetricsGauge(ApplicationMetricFields.CPU)

    private val ramGauge = applicationContainerMetricsGauge(ApplicationMetricFields.RAM)

    private val instanceCountGauge = applicationContainerMetricsGauge(ApplicationMetricFields.INSTANCES)

    private val requestsGauge = applicationContainerMetricsGauge(ApplicationMetricFields.REQUESTS)

    private val latencyGauge = applicationContainerMetricsGauge(ApplicationMetricFields.LATENCY)

    private val quotientGauge = applicationContainerMetricsGauge(ApplicationMetricFields.QUOTIENT)

    private fun applicationContainerMetricsGauge(applicationMetricFields: ApplicationMetricFields): Gauge {
        return Gauge.build(applicationMetricFields.applicationMetric, DESCRIPTION)
                .labelNames(APP_ID_LABEL_NAME)
                .register(containerMetricRegistry)
    }

    /**
     * Scaling Event Metrics
     */
    private val oldInstanceCountGauge = scalingEventMetrics(ScalingFields.OLD_INSTANCE_COUNT)

    private val newInstanceCountGauge = scalingEventMetrics(ScalingFields.NEW_INSTANCE_COUNT)

    private val currentMaxInstanceLimitGauge = scalingEventMetrics(ScalingFields.MAX_INSTANCE_LIMIT)

    private val currentMinInstanceLimitGauge = scalingEventMetrics(ScalingFields.MIN_INSTANCE_LIMIT)

    private val cpuLoadGauge = scalingEventMetrics(ScalingFields.CPU_LOAD)

    private val cpuUpperLimitGauge = scalingEventMetrics(ScalingFields.CPU_UPPER_LIMIT)

    private val cpuLowerLimitGauge = scalingEventMetrics(ScalingFields.CPU_LOWER_LIMIT)

    private val ramLoadGauge = scalingEventMetrics(ScalingFields.RAM_LOAD)

    private val ramUpperLimitGauge = scalingEventMetrics(ScalingFields.RAM_UPPER_LIMIT)

    private val ramLowerLimitGauge = scalingEventMetrics(ScalingFields.RAM_LOWER_LIMIT)

    private val requestCountGauge = scalingEventMetrics(ScalingFields.REQUEST_COUNT)

    private val latencyValueGauge = scalingEventMetrics(ScalingFields.LATENCY_VALUE)

    private val latencyUpperLimitGauge = scalingEventMetrics(ScalingFields.LATENCY_UPPER_LIMIT)

    private val latencyLowerLimitGauge = scalingEventMetrics(ScalingFields.LATENCY_LOWER_LIMIT)

    private val quotientValueGauge = scalingEventMetrics(ScalingFields.QUOTIENT_VALUE)

    private fun scalingEventMetrics(scalingFields: ScalingFields): Gauge {
        return Gauge
                .build(scalingFields.scalingField, DESCRIPTION)
                .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
                .register(applicationRegistry)
    }

    companion object {
        const val DESCRIPTION = "None"
        const val COMPONENT_LABEL_NAME = "component"
        const val APP_ID_LABEL_NAME = "app_id"
        const val APP_INSTANCE = "app_instance"
    }

    @PostConstruct
    fun executePrometheusWriter() {
        containerMetricConsumerRunner()
        applicationMetricConsumerRunner()
        scalingLogConsumerRunner()
        httpMetricConsumerRunner()
        pushGateway = PushGateway(prometheusPropertiesBean.host + ":" + prometheusPropertiesBean.port)

    }

    private fun containerMetricConsumerRunner() {
        val containerMetricConsumer: MutableList<InstanceMetricConsumer> = mutableListOf()
        for (i in 1 until kafkaPropertiesBean.containerConsumerCount) {
            containerMetricConsumer.add(InstanceMetricConsumer("writer_container_metric",
                    kafkaPropertiesBean, this))

            containerMetricConsumer[i-1].startConsumer()
        }
    }

    private fun applicationMetricConsumerRunner() {
        val applicationContainerMetricConsumer = ApplicationMetricConsumer("writer_application_container_metric",
                kafkaPropertiesBean, this)
        applicationContainerMetricConsumer.startConsumer()
    }

    private fun scalingLogConsumerRunner() {
        val scalingConsumer = ScalingLogConsumer("writer_scaling",
                kafkaPropertiesBean, this)
        scalingConsumer.startConsumer()
    }

    private fun httpMetricConsumerRunner() {
        val httpMetricConsumer = HttpMetricConsumer("writer_http_metric",
                kafkaPropertiesBean, this)
        httpMetricConsumer.startConsumer()
    }

    fun writeHttpMetric(data: HttpMetric) {
        httpRequestsGauge.labels(data.appId)
                .set(data.requests.toDouble())
        httpLatencyGauge.labels(data.appId)
                .set(data.latency.toDouble())

        pushGateway.pushAdd(httpRegistry, "http_metrics")
    }

    // Todo: Ask Marius why this is there?
    fun writeInstanceContainerMetric(data: ContainerMetric) {
        cpuInstanceGauge.labels(data.instanceIndex.toString(), data.appId)
                .set(data.cpu.toDouble())
        ramInstanceGauge.labels(data.instanceIndex.toString(), data.appId)
                .set(data.ram.toDouble())

        pushGateway.pushAdd(instanceContainerRegistry, "instance_container_metrics")
    }

    fun writeApplicationContainerMetric(data: ApplicationMetric) {
        cpuGauge.labels(data.appId)
                .set(data.cpu.toDouble())
        ramGauge.labels(data.appId)
                .set(data.ram.toDouble())
        instanceCountGauge.labels(data.appId)
                .set(data.instanceCount.toDouble())
        requestsGauge.labels(data.appId)
                .set(data.requests.toDouble())
        latencyGauge.labels(data.appId)
                .set(data.latency.toDouble())
        quotientGauge.labels(data.appId)
                .set(data.quotient.toDouble())

        pushGateway.pushAdd(containerMetricRegistry, "application_container_metrics")
    }

    fun writeScalingLog(data: ScalingLog) {
        // Todo: Ask Marius what measurement is about and where the value should be stored
        val measurement = prometheusPropertiesBean.scalingMeasurement
        var component = "undefined"
        when (data.component) {
            ScalingLog.HTTP_REQUEST_BASED -> component = "requests"
            ScalingLog.HTTP_LATENCY_BASED -> component = "latency"
            ScalingLog.CONTAINER_CPU_BASED -> component = "cpu"
            ScalingLog.CONTAINER_RAM_BASED -> component = "ram"
            ScalingLog.PREDICTOR_BASED -> component = "predictor"
            ScalingLog.LIMIT_BASED -> component = "limit"
        }

        oldInstanceCountGauge.labels(component, data.appId)
                .set(data.oldInstances.toDouble())
        newInstanceCountGauge.labels(component, data.appId)
                .set(data.newInstances.toDouble())
        currentMaxInstanceLimitGauge.labels(component, data.appId)
                .set(data.currentMaxInstanceLimit.toDouble())
        currentMinInstanceLimitGauge.labels(component, data.appId)
                .set(data.currentMinInstanceLimit.toDouble())
        cpuLoadGauge.labels(component, data.appId)
                .set(data.currentCpuLoad.toDouble())
        cpuUpperLimitGauge.labels(component, data.appId)
                .set(data.currentCpuUpperLimit.toDouble())
        cpuLowerLimitGauge.labels(component, data.appId)
                .set(data.currentCpuLowerLimit.toDouble())
        ramLoadGauge.labels(component, data.appId)
                .set(data.currentRamLoad.toDouble())
        ramUpperLimitGauge.labels(component, data.appId)
                .set(data.currentRamUpperLimit.toDouble())
        ramLowerLimitGauge.labels(component, data.appId)
                .set(data.currentRamLowerLimit.toDouble())
        requestCountGauge.labels(component, data.appId)
                .set(data.currentRequestCount.toDouble())
        latencyValueGauge.labels(component, data.appId)
                .set(data.currentLatencyValue.toDouble())
        latencyUpperLimitGauge.labels(component, data.appId)
                .set(data.currentLatencyUpperLimit.toDouble())
        latencyLowerLimitGauge.labels(component, data.appId)
                .set(data.currentLatencyLowerLimit.toDouble())
        quotientValueGauge.labels(component, data.appId)
                .set(data.currentQuotientValue.toDouble())

        pushGateway.pushAdd(applicationRegistry, "scaling_events")
    }

}
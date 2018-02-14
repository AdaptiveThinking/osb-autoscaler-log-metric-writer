package de.cf.autoscaler.prometheus.prometheus

import de.cf.autoscaler.kafka.KafkaPropertiesBean
import de.cf.autoscaler.kafka.messages.ApplicationMetric
import de.cf.autoscaler.kafka.messages.ContainerMetric
import de.cf.autoscaler.kafka.messages.HttpMetric
import de.cf.autoscaler.kafka.messages.ScalingLog
import de.cf.autoscaler.prometheus.beans.PrometheusPropertiesBean
import de.cf.autoscaler.prometheus.constants.ApplicationMetricFields
import de.cf.autoscaler.prometheus.constants.HttpMetricFields
import de.cf.autoscaler.prometheus.constants.InstanceMetricFields
import de.cf.autoscaler.prometheus.constants.ScalingFields
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

    private lateinit var pushGateway: PushGateway

    private val registry = CollectorRegistry()

    /**
     * Http Metrics
     */
    private val httpRequestsGauge = Gauge
            .build(HttpMetricFields.REQUESTS.httpMetric, DESCRIPTION)
            .labelNames(APP_ID_LABEL_NAME)
            .register(registry)

    private val httpLatencyGauge = Gauge
            .build(HttpMetricFields.LATENCY.httpMetric, DESCRIPTION)
            .labelNames(APP_ID_LABEL_NAME)
            .register(registry)

    /**
     * Instance Container Metrics
     */
    private val cpuInstanceGauge = Gauge
            .build(InstanceMetricFields.CPU.instanceMetric, DESCRIPTION)
            .labelNames(APP_INSTANCE, APP_ID_LABEL_NAME)
            .register(registry)

    private val ramInstanceGauge = Gauge
            .build(InstanceMetricFields.CPU.instanceMetric, DESCRIPTION)
            .labelNames(APP_INSTANCE, APP_ID_LABEL_NAME)
            .register(registry)

    /**
     * Application Container Metrics
     */
    private val cpuGauge = Gauge
            .build(ApplicationMetricFields.CPU.applicationMetric, DESCRIPTION)
            .labelNames(APP_ID_LABEL_NAME)
            .register(registry)

    private val ramGauge = Gauge
            .build(ApplicationMetricFields.RAM.applicationMetric, DESCRIPTION)
            .labelNames(APP_ID_LABEL_NAME)
            .register(registry)

    private val instanceCountGauge = Gauge
            .build(ApplicationMetricFields.INSTANCES.applicationMetric, DESCRIPTION)
            .labelNames(APP_ID_LABEL_NAME)
            .register(registry)

    private val requestsGauge = Gauge
            .build(ApplicationMetricFields.REQUESTS.applicationMetric, DESCRIPTION)
            .labelNames(APP_ID_LABEL_NAME)
            .register(registry)

    private val latencyGauge = Gauge
            .build(ApplicationMetricFields.LATENCY.applicationMetric, DESCRIPTION)
            .labelNames(APP_ID_LABEL_NAME)
            .register(registry)

    private val quotientGauge = Gauge
            .build(ApplicationMetricFields.QUOTIENT.applicationMetric, DESCRIPTION)
            .labelNames(APP_ID_LABEL_NAME)
            .register(registry)

    /**
     * Scaling Event Metrics
     */
    private val oldInstanceCountGauge = Gauge
            .build(ScalingFields.OLD_INSTANCE_COUNT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val newInstanceCountGauge = Gauge
            .build(ScalingFields.NEW_INSTANCE_COUNT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val currentMaxInstanceLimitGauge = Gauge
            .build(ScalingFields.MAX_INSTANCE_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val currentMinInstanceLimitGauge = Gauge
            .build(ScalingFields.MIN_INSTANCE_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val cpuLoadGauge = Gauge
            .build(ScalingFields.CPU_LOAD.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val cpuUpperLimitGauge = Gauge
            .build(ScalingFields.CPU_UPPER_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val cpuLowerLimitGauge = Gauge
            .build(ScalingFields.CPU_LOWER_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val ramLoadGauge = Gauge
            .build(ScalingFields.RAM_LOAD.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val ramUpperLimitGauge = Gauge
            .build(ScalingFields.RAM_UPPER_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val ramLowerLimitGauge = Gauge
            .build(ScalingFields.RAM_LOWER_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val requestCountGauge = Gauge
            .build(ScalingFields.REQUEST_COUNT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val latencyValueGauge = Gauge
            .build(ScalingFields.LATENCY_VALUE.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val latencyUpperLimitGauge = Gauge
            .build(ScalingFields.LATENCY_UPPER_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val latencyLowerLimitGauge = Gauge
            .build(ScalingFields.LATENCY_LOWER_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val quotientValueGauge = Gauge
            .build(ScalingFields.QUOTIENT_VALUE.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)

    private val latencyLowerLimit = Gauge
            .build(ScalingFields.LATENCY_LOWER_LIMIT.scalingField, DESCRIPTION)
            .labelNames(COMPONENT_LABEL_NAME, APP_ID_LABEL_NAME)
            .register(registry)


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
        pushGateway = PushGateway(prometheusPropertiesBean.host + ":" + prometheusPropertiesBean.port)

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

    fun writeHttpMetric(data: HttpMetric) {
        if (data != null) {
            httpRequestsGauge.labels(data.appId)
                    .set(data.requests.toDouble())
            httpLatencyGauge.labels(data.appId)
                    .set(data.latency.toDouble())

            pushGateway.pushAdd(registry, "http_metrics")
        }
    }

    // Todo: Ask Marius why this is there?
    fun writeInstanceContainerMetric(data: ContainerMetric) {
        if (data != null) {
            cpuInstanceGauge.labels(data.instanceIndex.toString(), data.appId)
                    .set(data.cpu.toDouble())
            ramInstanceGauge.labels(data.instanceIndex.toString(), data.appId)
                    .set(data.ram.toDouble())

            pushGateway.pushAdd(registry, "instance_container_metrics")
        }
    }

    fun writeApplicationContainerMetric(data: ApplicationMetric) {
        if (data != null) {
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

            pushGateway.pushAdd(registry, "application_container_metrics")
        }
    }

    fun writeScalingLog(data: ScalingLog) {
        if (data != null) {
            // Todo: Ask Marius what measurement is about and where the value should be stored
            val measurement = prometheusPropertiesBean.scalingMeasurement
            var component: String = "undefined"
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

            pushGateway.pushAdd(registry, "scaling_events")
        }
    }

}
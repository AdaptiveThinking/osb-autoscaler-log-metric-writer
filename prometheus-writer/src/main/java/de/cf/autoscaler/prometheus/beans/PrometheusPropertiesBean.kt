package de.cf.autoscaler.prometheus.beans

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "prometheus")
open class PrometheusPropertiesBean {

    lateinit var host: String

    var port: Int = 9091

    @Value("\${metric_http_measurement}")
    lateinit var metricHttpMeasurement: String

    @Value("\${metric_container_instance_measurement}")
    lateinit var metricContainerInstanceMeasurement: String

    @Value("\${metric_container_application_measurement}")
    lateinit var metricContainerApplicationMeasurement: String

    @Value("\${scaling_measurement}")
    lateinit var scalingMeasurement: String

}
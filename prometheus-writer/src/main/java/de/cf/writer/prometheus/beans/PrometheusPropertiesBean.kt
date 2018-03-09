package de.cf.writer.prometheus.beans

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "prometheus")
open class PrometheusPropertiesBean {

    lateinit var host: String

    var port: Int = 9091

    lateinit var metricHttpMeasurement: String

    lateinit var metricContainerInstanceMeasurement: String

    lateinit var metricContainerApplicationMeasurement: String

    lateinit var scalingMeasurement: String

}
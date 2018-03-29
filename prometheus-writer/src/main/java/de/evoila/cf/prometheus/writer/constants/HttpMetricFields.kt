package de.evoila.cf.prometheus.writer.constants

enum class HttpMetricFields(val httpMetric: String) {

    REQUESTS("requests"),
    LATENCY("latency"),
    DESCRIPTION("description")

}
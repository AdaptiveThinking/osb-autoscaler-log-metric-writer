package de.cf.writer.prometheus.constants

enum class HttpMetricFields(val httpMetric: String) {

    REQUESTS("requests"),
    LATENCY("latency"),
    DESCRIPTION("description")

}
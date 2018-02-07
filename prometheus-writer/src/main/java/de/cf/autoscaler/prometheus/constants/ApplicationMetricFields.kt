package de.cf.autoscaler.prometheus.constants

enum class ApplicationMetricFields(val applicationMetric: String) {

    CPU("cpu"),
    RAM("ram"),
    INSTANCES("instances"),
    REQUESTS("requests"),
    LATENCY("latency"),
    QUOTIENT("quotient"),
    DESCRIPTION("description")

}
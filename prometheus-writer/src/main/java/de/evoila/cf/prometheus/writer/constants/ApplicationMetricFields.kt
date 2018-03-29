package de.evoila.cf.prometheus.writer.constants

enum class ApplicationMetricFields(val applicationMetric: String) {

    CPU("cpu"),
    RAM("ram"),
    INSTANCES("instances"),
    REQUESTS("requests"),
    LATENCY("latency"),
    QUOTIENT("quotient"),
    DESCRIPTION("description")

}
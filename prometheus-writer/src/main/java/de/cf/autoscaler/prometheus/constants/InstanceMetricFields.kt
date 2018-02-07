package de.cf.autoscaler.prometheus.constants

enum class InstanceMetricFields(val instanceMetric: String) {

    CPU("cpu"),
    RAM("ram"),
    DESCRIPTION("description")

}
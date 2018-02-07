package de.cf.autoscaler.prometheus.constants

enum class ScalingFields(val scalingField: String) {

    OLD_INSTANCE_COUNT("oldInstanceCount"),
    NEW_INSTANCE_COUNT("newInstanceCount"),
    MAX_INSTANCE_LIMIT("maxInstanceLimit"),
    MIN_INSTANCE_LIMIT("minInstanceLimit"),
    CPU_LOAD("cpuLoad"),
    CPU_UPPER_LIMIT("cpuUpperLimit"),
    CPU_LOWER_LIMIT("cpuLowerLimit"),
    RAM_LOAD("ramLoad"),
    RAM_UPPER_LIMIT("ramUpperLimit"),
    RAM_LOWER_LIMIT("ramLowerLimit"),
    REQUEST_COUNT("requestCount"),
    LATENCY_VALUE("latencyValue"),
    LATENCY_UPPER_LIMIT("latencyUpperLimit"),
    LATENCY_LOWER_LIMIT("latencyLowerLimit"),
    QUOTIENT_VALUE("quotientValue"),
    DESCRIPTION("description")

}
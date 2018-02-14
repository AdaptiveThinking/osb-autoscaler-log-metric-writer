package de.cf.autoscaler.prometheus.constants

enum class ScalingFields(val scalingField: String) {

    OLD_INSTANCE_COUNT("autoscaler_scaling_old_instance_count"),
    NEW_INSTANCE_COUNT("autoscaler_scaling_new_instance_count"),
    MAX_INSTANCE_LIMIT("autoscaler_scaling_max_instance_limit"),
    MIN_INSTANCE_LIMIT("autoscaler_scaling_min_instance_limit"),
    CPU_LOAD("autoscaler_scaling_cpu_load"),
    CPU_UPPER_LIMIT("autoscaler_scaling_cpu_upper_limit"),
    CPU_LOWER_LIMIT("autoscaler_scaling_cpu_lower_limit"),
    RAM_LOAD("autoscaler_scaling_ram_load"),
    RAM_UPPER_LIMIT("autoscaler_scaling_ram_upper_limit"),
    RAM_LOWER_LIMIT("autoscaler_scaling_ram_lower_limit"),
    REQUEST_COUNT("autoscaler_scaling_request_count"),
    LATENCY_VALUE("autoscaler_scaling_latency_value"),
    LATENCY_UPPER_LIMIT("autoscaler_scaling_latency_upper_limit"),
    LATENCY_LOWER_LIMIT("autoscaler_scaling_latency_lower_limit"),
    QUOTIENT_VALUE("autoscaler_scaling_quotient_value"),
    DESCRIPTION("autoscaler_scaling_description")

}
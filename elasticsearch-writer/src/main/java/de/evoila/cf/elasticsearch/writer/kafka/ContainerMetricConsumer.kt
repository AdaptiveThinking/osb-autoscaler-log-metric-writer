package de.evoila.cf.elasticsearch.writer.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.ContainerMetric
import de.evoila.cf.elasticsearch.ElasticsearchWriter

class ContainerMetricConsumer(groupId: String, kafkaPropertiesBean: KafkaPropertiesBean,
                              private val writer: ElasticsearchWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread = ByteConsumerThread(kafkaPropertiesBean.metricContainerTopic,
            groupId,
            this,
            kafkaPropertiesBean)

    override fun startConsumer() {
        consThread.start()
    }

    override fun stopConsumer() {
        consThread.kafkaConsumer.wakeup()
    }

    override fun consume(bytes: ByteArray) {
        val metric = ObjectMapper().readValue(bytes, ContainerMetric::class.java)
        writer.writeMetric(metric, "containermetrics")
    }

    override fun getType(): String {
        return "logMetric_metric_container"
    }

}

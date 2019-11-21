package de.evoila.cf.elasticsearch.writer.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.HttpMetric
import de.evoila.cf.elasticsearch.ElasticsearchWriter

class HttpMetricConsumer(groupId: String, kafkaPropertiesBean: KafkaPropertiesBean,
                         private val writer: ElasticsearchWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread = ByteConsumerThread(kafkaPropertiesBean.metricHttpTopic,
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
        val metric = ObjectMapper().readValue(bytes, HttpMetric::class.java)
        writer.writeMetric(metric, "httpmetrics")
    }

    override fun getType(): String {
        return "metric_http"
    }

    override fun removeConsumer(topicName: String?) {}
}

package de.evoila.cf.elasticsearch.writer.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.autoscaler.kafka.messages.ApplicationMetric
import de.evoila.cf.elasticsearch.ElasticsearchWriter

class ApplicationMetricConsumer(groupId: String, kafkaPropertiesBean: KafkaPropertiesBean,
                                private val writer: ElasticsearchWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread = ByteConsumerThread(kafkaPropertiesBean.metricApplicationTopic,
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
        val metric = ObjectMapper().readValue(bytes, ApplicationMetric::class.java)
        writer.writeMetric(metric, "applicationmetrics")
    }

    override fun getType(): String {
        return "metric_application"
    }

    override fun removeConsumer(topicName: String?) {}
}

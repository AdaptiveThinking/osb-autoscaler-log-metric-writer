package de.evoila.cf.elasticsearch.writer.kafka

import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer
import de.evoila.cf.autoscaler.kafka.ByteConsumerThread
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean
import de.evoila.cf.elasticsearch.ElasticsearchWriter

/**
 * Created by reneschollmeyer, evoila on 29.01.19.
 */
class ServerConfigConsumer(groupId: String, kafkaPropertiesBean: KafkaPropertiesBean,
                         private val writer: ElasticsearchWriter) : AutoScalerConsumer {

    private val consThread: ByteConsumerThread = ByteConsumerThread("server_config",
            groupId,
            this,
            kafkaPropertiesBean)

    override fun startConsumer() {
        consThread.start()
    }

    override fun stopConsumer() {
        consThread.kafkaConsumer.wakeup()
    }

    override fun consume(bytes: ByteArray?) {
       bytes?.let{writer.writeJson(it, "serverconfig")}
    }

    override fun getType(): String {
        return "server_config"
    }
}
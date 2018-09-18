package de.evoila.cf.elasticsearch.writer.model

/**
 * Created by reneschollmeyer, evoila on 19.03.18.
 */
class ElasticsearchWriterObject(val timestamp: Long,
                                val latency: Long,
                                var logMessage: String,
                                val logMessageType: String,
                                val sourceType: String,
                                val appId: String,
                                val appName: String,
                                val space: String,
                                val organization: String,
                                val organization_guid: String,
                                val sourceInstance: String) {


}
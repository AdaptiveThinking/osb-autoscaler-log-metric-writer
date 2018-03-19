package de.cf.writer.elasticsearch.model

import java.sql.Date

/**
 * Created by reneschollmeyer, evoila on 19.03.18.
 */
class ElasticsearchWriterObject(val timestamp: Date,
                                val logMessage: String,
                                val logMessageType: String,
                                val appId: String) {


}
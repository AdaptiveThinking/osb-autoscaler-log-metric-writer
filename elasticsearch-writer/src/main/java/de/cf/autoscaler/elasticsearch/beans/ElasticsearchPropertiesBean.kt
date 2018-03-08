package de.cf.autoscaler.elasticsearch.beans

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by reneschollmeyer, evoila on 07.03.18.
 */

@ConfigurationProperties(prefix = "elasticsearch")
open class ElasticsearchPropertiesBean {

    lateinit var host: String

    var port: Int = 9300

    lateinit var clusterName: String
}
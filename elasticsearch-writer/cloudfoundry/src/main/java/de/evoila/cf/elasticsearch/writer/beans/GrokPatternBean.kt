package de.evoila.cf.elasticsearch.writer.beans

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by reneschollmeyer, evoila on 28.05.18.
 */
@ConfigurationProperties(prefix = "grok")
open class GrokPatternBean {

    lateinit var pattern: String
}
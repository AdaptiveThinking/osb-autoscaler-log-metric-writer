package de.evoila.cf.elasticsearch.writer.beans

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/**
 * Created by reneschollmeyer, evoila on 24.09.19.
 */
@Service
@Profile("!pcf")
@ConfigurationProperties(prefix = "redis")
class RedisBean {

    var hosts: List<String>? = null
    var port: Int = 0
    var password: String? = null
}

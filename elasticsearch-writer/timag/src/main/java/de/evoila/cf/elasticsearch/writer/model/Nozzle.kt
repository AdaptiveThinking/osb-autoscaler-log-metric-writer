package de.evoila.cf.elasticsearch.writer.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by reneschollmeyer, evoila on 12.07.19.
 */
class Nozzle {

    @JsonProperty("NozzleId")
    var nozzleId: String? = null

    @JsonProperty("Endpoints")
    var endpoints: List<String>? = null
}

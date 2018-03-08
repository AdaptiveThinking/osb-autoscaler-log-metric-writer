package de.cf.autoscaler.prometheus.beans

import java.io.Serializable

/**
 *
 * @author Marius Berger
 */
data class Credentials(var hostname: String?, var port: Int, var username: String?, var password: String?) : Serializable {

    val fullHost: String
        get() = hostname + ":" + port

}

package de.cf.autoscaler.prometheus.beans

import java.io.Serializable

/**
 *
 * @author Marius Berger
 */
class Credentials(var hostname: String?, var port: Int, var username: String?, var password: String?) : Serializable {

    val fullHost: String
        get() = hostname + ":" + port

    companion object {

        /**
         *
         */
        private const val serialVersionUID = -6516455128811362745L
    }
}

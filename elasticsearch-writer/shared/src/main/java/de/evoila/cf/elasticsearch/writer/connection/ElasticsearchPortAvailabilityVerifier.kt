package de.evoila.cf.elasticsearch.security

/**
 * Created by reneschollmeyer, evoila on 20.12.18.
 */
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/**
 * @author Johannes Hiemer.
 */
@Service
class ElasticsearchPortAvailabilityVerifier {

    private val log = LoggerFactory.getLogger(ElasticsearchPortAvailabilityVerifier::class.java)

    fun timeout(timeout: Int) {
        try {
            Thread.sleep(timeout.toLong())
        } catch (e1: InterruptedException) {
            log.info("Starting new timeout interval was interrupted.", e1)
        }

    }

    private fun execute(ip: String, port: Int): Boolean {
        var available = false

        log.info("Verifying port availability on: {}:{}", ip, port)
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(ip, port), SOCKET_TIMEOUT)

            if (socket.isConnected)
                available = true
            else
                timeout(SOCKET_TIMEOUT)
        } catch (e: Exception) {
            log.info("Service port could not be reached", e)

            timeout(SOCKET_TIMEOUT)
        } finally {
            if (socket.isConnected) {
                try {
                    socket.close()
                } catch (e: IOException) {
                    log.info("Could not close port", e)
                }

            }
        }
        return available
    }

    fun verifyServiceAvailability(ip: String, port: Int, useInitialTimeout: Boolean): Boolean {
        var available = false

        if (useInitialTimeout)
            this.timeout(INITIAL_TIMEOUT)

        available = this.execute(ip, port)

        log.info("Service Port availability: {}", available)

        log.info("Service Port availability (last status during request): {}", available)
        return available
    }

    companion object {

        private val SOCKET_TIMEOUT = 30000

        private val INITIAL_TIMEOUT = 150 * 1000
    }

}
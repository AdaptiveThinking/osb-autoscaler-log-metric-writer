package de.cf.autoscaler.prometheus

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.util.Assert

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger

@ComponentScan
object WriterApp {


    @JvmStatic
    fun main(args: Array<String>) {
        val ctx = AnnotationConfigApplicationContext(WriterConfiguration::class.java)

        // ---- Disable log spam for convenience ----
        var log = LoggerFactory.getLogger(org.apache.kafka.clients.consumer.internals.Fetcher::class.java!!) as Logger
        log.level = Level.INFO

        log = LoggerFactory.getLogger(org.apache.kafka.clients.NetworkClient::class.java!!) as Logger
        log.level = Level.INFO

        log = LoggerFactory.getLogger(org.apache.kafka.clients.consumer.internals.ConsumerCoordinator::class.java!!) as Logger
        log.level = Level.INFO

        log = LoggerFactory.getLogger(org.apache.kafka.clients.consumer.internals.AbstractCoordinator::class.java!!) as Logger
        log.level = Level.INFO
    }

}

package de.evoila.cf.autoscaler

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class WriterApp

fun main(args: Array<String>) {
    SpringApplication.run(WriterApp::class.java, *args)

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
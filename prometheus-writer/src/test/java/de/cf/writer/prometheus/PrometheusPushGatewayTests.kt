package de.cf.writer.prometheus

import de.cf.writer.prometheus.constants.ScalingFields
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import java.util.ArrayList
import java.util.concurrent.TimeUnit

import io.prometheus.client.exporter.PushGateway
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PrometheusPushGatewayTests {

    private val registry = CollectorRegistry()
    private var host: String = "10.244.0.6"
    private val port: Int = 9091

    lateinit var pushGateway: PushGateway

    @Before
    fun setUp() {
        pushGateway = PushGateway(host + ":" + port)
    }

    @After
    fun cleanup() {}

    @Test
    fun `write a Gauge value to the Push Gateway`() {
        val oldInstanceCountGauge = Gauge
                .build(ScalingFields.OLD_INSTANCE_COUNT.scalingField, "None yet")
                .labelNames("component", "app_id")
                .register(registry)

        for (i in 1..1000) {
            Thread.sleep(5000)
            oldInstanceCountGauge.labels("count", "612ceed0-ab19-4b35-b8ab-32af8f2e7cb7")
                    .set(Math.random().toDouble())

            oldInstanceCountGauge.labels("count", "fw232ed0-aad23-f5g5-b8ab-32a4r4sdf7cb7")
                    .set(Math.random().toDouble())


            pushGateway.pushAdd(registry, "running_batch_job")
        }
    }

}

package de.evoila.cf.elasticsearch.writer.redis

import de.evoila.cf.elasticsearch.writer.beans.RedisBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisCluster
import redis.clients.jedis.JedisPoolConfig
import java.io.IOException
import java.util.*

/**
 * Created by reneschollmeyer, evoila on 06.09.18.
 */
@Service
@ConditionalOnBean(RedisBean::class)
class RedisClientConnector(private val redisBean: RedisBean) {

    private var jedis: Jedis? = null

    private var jedisCluster: JedisCluster? = null

    private fun redisSingleNodeConnection(): Jedis {
        val jedis = Jedis(redisBean.hosts!![0], redisBean.port)
        jedis.connect()
        jedis.auth(redisBean.password)

        return jedis
    }

    private fun redisClusterConnection(): JedisCluster {

        val jedisClusterNodes = HashSet<HostAndPort>()

        for (host in redisBean.hosts!!) {
            jedisClusterNodes.add(HostAndPort(host, redisBean.port))
        }

        val poolConfig = JedisPoolConfig()
        poolConfig.maxTotal = 20
        poolConfig.maxIdle = 3
        poolConfig.maxWaitMillis = 3000
        jedisCluster = JedisCluster(jedisClusterNodes, 100, 3, 100, redisBean.password, poolConfig)

        return jedisCluster as JedisCluster
    }

    operator fun set(key: String, value: String) {
        jedis = redisSingleNodeConnection()

        if (jedis!!.info("Cluster").contains(isCluster)) {
            jedis!!.close()
            clusterSet(key, value)
        } else {
            jedis!!.set(key, value)
            jedis!!.close()
        }
    }

    private fun clusterSet(key: String, value: String) {
        jedisCluster = redisClusterConnection()
        jedisCluster!!.set(key, value)

        try {
            jedisCluster!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    operator fun get(key: String): String? {
        jedis = redisSingleNodeConnection()

        return if (jedis!!.info("Cluster").contains(isCluster)) {
            jedis!!.close()
            clusterGet(key)
        } else {
            val value = jedis!!.get(key)
            jedis!!.close()
            value
        }
    }

    private fun clusterGet(key: String): String {
        jedisCluster = redisClusterConnection()
        val value = jedisCluster!!.get(key)

        try {
            jedisCluster!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return value
    }

    fun del(key: String) {
        jedis = redisSingleNodeConnection()

        if (jedis!!.info("Cluster").contains(isCluster)) {
            jedis!!.close()
            clusterDel(key)
        } else {
            jedis!!.del(key)
            jedis!!.close()
        }
    }

    private fun clusterDel(key: String) {
        jedisCluster = redisClusterConnection()
        jedisCluster!!.del(key)

        try {
            jedisCluster!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {

        private val isCluster = "cluster_enabled:1"
    }
}
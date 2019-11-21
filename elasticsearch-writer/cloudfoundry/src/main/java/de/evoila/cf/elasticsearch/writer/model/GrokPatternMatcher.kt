package de.evoila.cf.elasticsearch.writer.model

import de.evoila.cf.elasticsearch.writer.beans.GrokPatternBean
import io.krakens.grok.api.Grok
import io.krakens.grok.api.GrokCompiler

/**
 * Created by reneschollmeyer, evoila on 28.05.18.
 */
class GrokPatternMatcher(grokPatternBean: GrokPatternBean){

    private var grokCompiler: GrokCompiler = GrokCompiler.newInstance()
    private var grok: Grok

    init {
        grokCompiler.registerDefaultPatterns()
        grok = grokCompiler.compile(grokPatternBean.pattern, true)
    }

    fun match(logMessage: String): Map<String, Any> {

        val match = grok.match(logMessage)

        return match.capture().minus("PORT")
    }
}
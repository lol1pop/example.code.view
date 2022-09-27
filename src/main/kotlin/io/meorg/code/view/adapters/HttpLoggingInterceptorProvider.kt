package io.meorg.code.view.adapters

import mu.KotlinLogging
import okhttp3.logging.HttpLoggingInterceptor

class HttpLoggingInterceptorProvider {
    private val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            logger.info { message }
        }
    })

    fun setLoggerLevel(level: HttpLoggingInterceptor.Level) {
        interceptor.level = level
    }

    fun getLogger() = interceptor

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
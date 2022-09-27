package io.meorg.code.view.adapters


import io.meorg.code.view.adapters.HttpLoggingInterceptorProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class OkHttpConfig {
    @Bean
    fun loggerProvider() = HttpLoggingInterceptorProvider()

    @Bean
    @Primary
    fun okHttpClient(httpLoggingInterceptorProvider: HttpLoggingInterceptorProvider): OkHttpClient {
        httpLoggingInterceptorProvider.setLoggerLevel(HttpLoggingInterceptor.Level.HEADERS)
        return OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptorProvider.getLogger())
                .build()
    }

    @Bean(name = ["no_logger"])
    fun okHttpClientNoLogger(): OkHttpClient = OkHttpClient.Builder().build()

}
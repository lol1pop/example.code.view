package com.switips.us.algopix

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.switips.us.algopix.response.webinterface.AlgopixWebResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.time.Duration
import java.util.concurrent.CompletableFuture
import net.besttoolbars.connectors.shared.*

interface AlgopixWebProductApi {

    @GET("items/identify")
    fun identify(
            @Header("Authorization") bearer: String,
            @Header("User-Agent") userAgent: String? = null,
            @Query("query") query: String,
            @Query("markets") markets: List<String>? = null,
            @Query("from") from: String? = null,
            @Query("shipping_method") shipping_method: String? = null,
            @Query("costCurrencyCode") costCurrencyCode: String? = null,
            @Query("itemConditions") itemConditions: String? = "New",
    ): CompletableFuture<AlgopixWebResponse>

    companion object {
        fun provider(url: String = "https://algopix.com/v2/",
                     client: OkHttpClient? = null,
                     config: RateLimitConfig = RateLimitConfig(25, Duration.ofMinutes(1))
        ): AlgopixWebProductApi {
            val objectMapper = ObjectMapper().registerModule(KotlinModule())
            val httpClient = provideHttpClientWithRateLimit(config, client)
            val retrofit = Retrofit.Builder()
                    .client(httpClient)
                    .baseUrl(url)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            return retrofit.build().create(AlgopixWebProductApi::class.java)
        }
    }
}

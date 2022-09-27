package com.switips.us.algopix

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.switips.us.algopix.response.product.AlgopixResponse
import com.switips.us.algopix.response.product.data.AlgopixResult
import com.switips.us.algopix.response.product.identification.AlgopixProductIdentification
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.CompletableFuture

interface AlgopixProductApi {

    @GET("/product/enhancement")
    fun enhancment(
            @Header("X-API-KEY") apiKey: String,
            @Header("X-APP-ID") appId: String,
            @Query("identifier") query: String,
            @Query("markets") markets: List<String>? = null,
            @Query("resources") resources: String? = null,
    ): CompletableFuture<AlgopixResponse<AlgopixResult>>

    @GET("/multiItemsSearch")
    fun search(
            @Header("X-API-KEY") apiKey: String,
            @Header("X-APP-ID") appId: String,
            @Query("keywords") keywords: String,
            @Query("brand") brand: String? = null,
            @Query("color") color: String? = null,
            @Query("googleGbCategoryId") googleGbCategoryId: Long? = null,
            @Query("amazonSearchIndex") amazonSearchIndex: String? = null,
            @Query("amazonCategoryId") amazonCategoryId: Long? = null,
            @Query("minPrice") minPrice: Double? = null,
            @Query("maxPrice") maxPrice: Double? = null,
            @Query("currencyCode") currencyCode: String? = null
    ): CompletableFuture<AlgopixResponse<AlgopixProductIdentification>>

    companion object {
        fun provider(url: String = "https://algopix.com/v3/", client: OkHttpClient? = null): AlgopixProductApi {
            val objectMapper = ObjectMapper().registerModule(KotlinModule())
            val retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            if (client != null) {
                retrofit.client(client)
            }
            return retrofit.build().create(AlgopixProductApi::class.java)
        }
    }
}

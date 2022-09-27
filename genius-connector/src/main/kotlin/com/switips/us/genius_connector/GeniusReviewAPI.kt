package com.switips.us.genius_connector

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.switips.us.genius_connector.response.review.GeniusReview
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.CompletableFuture

interface GeniusReviewAPI {
    @GET("/reviews")
    fun reviews(
            @Header("ApiGenius_API_Key") apiKey: String,
            @Query("query") query: String
    ): CompletableFuture<GeniusReview>

    companion object {
        fun provider(url: String = "https://api.apigenius.io/", client: OkHttpClient? = null): GeniusReviewAPI {
            val objectMapper = ObjectMapper().registerModule(KotlinModule())
            val retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            if (client != null) {
                retrofit.client(client)
            }
            return retrofit.build().create(GeniusReviewAPI::class.java)
        }
    }
}
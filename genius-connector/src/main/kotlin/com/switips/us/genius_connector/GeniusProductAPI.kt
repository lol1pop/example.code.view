package com.switips.us.genius_connector

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.switips.us.genius_connector.response.product.GeniusProduct
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.CompletableFuture

interface GeniusProductAPI {
    @GET("/products/identifiers")
    fun identifiers(
            @Header("ApiGenius_API_Key") apiKey: String,
            @Query("upc") upc: String? = null,
            @Query("mpn") mpn: String? = null,
            @Query("title") title: String? = null
    ): CompletableFuture<GeniusProduct>

    @GET("/products/lookup")
    fun lookup(
            @Header("ApiGenius_API_Key") apiKey: String,
            @Query("upc") upc: String
    ): CompletableFuture<GeniusProduct>

    @GET("/products/product-data")
    fun productData(
            @Header("ApiGenius_API_Key") apiKey: String
    ): CompletableFuture<GeniusProduct>

    @GET("/products/search")
    fun search(
            @Header("ApiGenius_API_Key") apiKey: String,
            @Query("keyword") keyword: String,
            @Query("title") title: String? = null,
            @Query("mpn") mpn: String? = null,
            @Query("category") category: String? = null,
            @Query("brand") brand: String? = null
    ): CompletableFuture<GeniusProduct>

    companion object {
        fun provider(url: String = "https://api.apigenius.io/", client: OkHttpClient? = null): GeniusProductAPI {
            val objectMapper = ObjectMapper().registerModule(KotlinModule())
            val retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            if (client != null) {
                retrofit.client(client)
            }
            return retrofit.build().create(GeniusProductAPI::class.java)
        }
    }
}
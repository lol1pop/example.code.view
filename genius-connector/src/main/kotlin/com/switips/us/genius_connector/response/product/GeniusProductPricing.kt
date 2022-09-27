package com.switips.us.genius_connector.response.product

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeniusProductPricing(
        val seller: String? = null,
        @JsonProperty("website_name")
        val websiteName: String? = null,
        val title: String? = null,
        val currency: String? = null,
        val price: Double? = null,
        val shipping: String? = null,
        val condition: String? = null,
        val link: String? = null,
        @JsonProperty("date_found")
        val dateFound: Long? = null
) {
    @delegate:JsonIgnore
    val dateFoundInstant by lazy { dateFound?.let { Instant.ofEpochMilli(it) } ?: Instant.EPOCH }
}
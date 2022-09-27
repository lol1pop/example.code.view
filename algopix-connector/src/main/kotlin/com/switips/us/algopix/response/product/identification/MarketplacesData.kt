package com.switips.us.algopix.response.product.identification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class Marketplaces(
        @JsonProperty("EBAY_US")
        val ebayUs: EbayUs? = null,

        @JsonProperty("AMAZON_US")
        val amazonUs: AmazonUs? = null
)

typealias ProductDetails = JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class AmazonUs(
        val title: String? = null,
        val features: List<String>? = null,
        val productDescription: String? = null,
        val productDetails: ProductDetails? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EbayUs(
        val title: String? = null,
        val features: List<String>? = null,
        val productDescription: String? = null,
        val productDetails: ProductDetails? = null
)

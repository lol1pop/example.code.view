package com.switips.us.genius_connector.response.product

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeniusProductItem(
        val title: String? = null,
        val brand: String? = null,
        @JsonProperty("part_number")
        val partNumber: String? = null,
        @JsonProperty("description")
        val description: String? = null,
        val ean: String? = null,
        val upc: String? = null,
        val mpn: String? = null,
        val elid: String? = null,
        val isbn: String? = null,
        val color: String? = null,
        val size: String? = null,
        val dimension: String? = null,
        val weight: String? = null,
        val currency: String? = null,
        @JsonProperty("lowest_pricing")
        val lowestPrice: Double? = null,
        @JsonProperty("highest_price")
        val highestPrice: Double? = null,
        val publisher: String? = null,
        val category: String? = null,
        val images: List<String>? = null,
        val pricing: List<GeniusProductPricing>? = null,
        val asin: String? = null,
        @JsonProperty("ebay_id")
        val ebayId: String? = null
)
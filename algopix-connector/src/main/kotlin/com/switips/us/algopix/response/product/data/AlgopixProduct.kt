package com.switips.us.algopix.response.product.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgopixProduct(
        val title: String? = null,
        val identifiers: Identifiers? = null,
        val matchingScores: MatchingScores? = null,
        val image: Image? = null,
        val price: Price? = null,
        val category: String? = null,
        val attributes: Attributes? = null,
        val listingUrl: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Attributes(
        @JsonProperty("Brand")
        val brand: String? = null,

        @JsonProperty("Size")
        val size: String? = null,

        @JsonProperty("Color")
        val color: String? = null,

        @JsonProperty("Manufacturer")
        val manufacturer: String? = null,

        @JsonProperty("Model")
        val model: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Identifiers(
        @JsonProperty("ASIN")
        val asin: List<String>? = null,

        @JsonProperty("UPC")
        val upc: List<String>? = null,

        @JsonProperty("EAN")
        val ean: List<String>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Image(
        val imageType: String? = null,
        val imageUrl: String? = null,
        val imageHeight: ImageDimension? = null,
        val imageWidth: ImageDimension? = null
)

data class ImageDimension(
        val value: Double,
        val unit: ImageDimensionUnit
)

enum class ImageDimensionUnit {
    PIXELS
}

data class MatchingScores(
        val titleMatchingScore: Long? = null,
        val brandMatchingScore: Long? = null,
        val colorMatchingScore: Long? = null,
        val categoryMatchingScore: Long? = null,
        val algopixTotalScore: Long? = null
)

data class Price(
        val currencyCode: String,
        val amount: Long
)

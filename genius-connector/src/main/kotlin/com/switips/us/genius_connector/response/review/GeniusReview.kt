package com.switips.us.genius_connector.response.review

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeniusReview(
        val success: Boolean? = null,
        val status: Int? = null,
        val title: String? = null,
        val sku: String? = null,
        val brand: String? = null,
        val reviews: List<GeniusReviewItem>? = null,
        val message: String? = null
) {
    @delegate:JsonIgnore
    val averageRating by lazy { reviews?.filter { !(it.rating?.isNaN() ?: true) }?.sumByDouble { it.rating ?: 0.0 } }
}
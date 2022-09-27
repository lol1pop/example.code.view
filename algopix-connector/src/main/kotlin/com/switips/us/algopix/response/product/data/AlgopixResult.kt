package com.switips.us.algopix.response.product.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgopixResult(
        val products: List<AlgopixProduct>? = null,
        val totalResults: Long
)

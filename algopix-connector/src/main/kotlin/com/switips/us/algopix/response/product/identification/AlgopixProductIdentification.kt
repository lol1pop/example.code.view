package com.switips.us.algopix.response.product.identification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgopixProductIdentification(
        val aid: String,
        val brand: String? = null,
        val model: String? = null,
        val color: String? = null,
        val description: String? = null,
        val itemDimensions: Dimensions? = null,
        val marketplacesData: Marketplaces? = null,                     //TODO ignored?

        @JsonProperty("algopixValidatedAttributes")
        val attributes: Attributes? = null,
)

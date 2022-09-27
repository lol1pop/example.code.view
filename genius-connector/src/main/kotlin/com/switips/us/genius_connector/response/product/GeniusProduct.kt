package com.switips.us.genius_connector.response.product

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeniusProduct(
        val success: Boolean? = null,
        val status: Int? = null,
        val identifier: String? = null,
        @JsonProperty("identifier_type")
        val identifierType: String? = null,
        val items: GeniusProductItem? = null,
        val message: String? = null
)
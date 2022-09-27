package com.switips.us.algopix.response.webinterface

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgopixBarCodes(
        val AID: Set<String>? = null,
        val EAN: Set<String>? = null,
        val UPC: Set<String>? = null,
        val ASIN: Set<String>? = null,
        val IDENTIFIED_KEYWORDS: Set<String>? = null
)

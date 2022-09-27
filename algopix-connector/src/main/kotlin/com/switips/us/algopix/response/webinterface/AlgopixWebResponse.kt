package com.switips.us.algopix.response.webinterface

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgopixWebResponse(
        val requestId: String,
        val statusCode: Long,
        val errorDetails: String,
        val data: ItemResult? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ItemResult(
        val marketItem: AlgopixDataItem? = null,
        val originalQuery: String,
)

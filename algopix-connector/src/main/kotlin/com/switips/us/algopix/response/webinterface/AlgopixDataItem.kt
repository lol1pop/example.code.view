package com.switips.us.algopix.response.webinterface

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.switips.us.algopix.response.product.data.Image
import com.switips.us.algopix.response.product.identification.Dimensions

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgopixDataItem(
        val allIds: AlgopixBarCodes? = null,
        val name: String? = null,
        val description: String? = null,
        val model: String? = null,
        val brand: String? = null,
        val color: String? = null,
        val size: String? = null,
        val manufacturer: String? = null,
        val largeImageUrl: String? = null,
        val algopixImagesSet: Set<Image>? = null,
        val algopixValidatedAttributes: WebAttributes? = null,
        val dimensions: Dimensions? = null,
        val packageDimensions: Dimensions? = null,
        val identifierIndication: String? = null,
        val market: String? = null,
        //val langToTitles: LangToTitles? = null
        //val marketCategory: MarketCategory? = null,
        //val marketItemCategories: FluffyMarketItemCategories? = null,
        //val msrp: Price? = null,
        //val featureList: List<Any?>? = null,
)

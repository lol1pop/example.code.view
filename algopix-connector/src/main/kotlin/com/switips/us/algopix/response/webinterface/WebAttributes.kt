package com.switips.us.algopix.response.webinterface

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.switips.us.algopix.response.product.identification.AlgopixUnitType

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebAttributes(
        val partNumber: String? = null,
        val releaseDate: String? = null,
        val cpuManufacturer: String? = null,
        val cpuSpeed: AlgopixUnitType? = null,
        val displaySize: AlgopixUnitType? = null,
        val hardDiskSize: AlgopixUnitType? = null,
        val hardwarePlatform: String? = null,
        val label: String? = null,
        val numberOfItems: Long? = null,
        val operatingSystems: List<String>? = null,
        val publisher: String? = null,
        val studio: String? = null,
        val systemMemorySize: AlgopixUnitType? = null,
        val title: String? = null,
        val warranty: String? = null,
        val lang: String? = null
)

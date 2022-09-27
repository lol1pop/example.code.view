package com.switips.us.algopix.response.product.identification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Attributes(
        val actors: List<String>? = null,
        val artist: String? = null,
        val aspectRatio: String? = null,
        val audienceRating: String? = null,
        val authors: List<String>? = null,
        val backFinding: String? = null,
        val bandMaterialType: String? = null,
        val blurayReigon: String? = null,
        val chainType: String? = null,
        val claspType: String? = null,
        val cpuManafacturer: String? = null,
        val algopixUnitType: AlgopixUnitType? = null,
        val cpuType: String? = null,
        val contributors: List<Contributor>? = null,
        val directors: List<String>? = null,
        val displaySize: AlgopixUnitType? = null,
        val edition: String? = null,
        val episodeSequence: String? = null,
        val esrbAgeRating: String? = null,
        val flavor: String? = null,
        val format: String? = null,
        val gemType: List<String>? = null,
        val genere: String? = null,
        val golfClubFlex: String? = null,
        val golfClubLoft: AlgopixUnitType? = null,
        val handOrientation: String? = null,
        val hardDiskInterface: String? = null,
        val hardDiskSize: AlgopixUnitType? = null,
        val hardwarePlatform: String? = null,
        val hazardousMaterialType: String? = null,
        val adultProduct: Boolean? = null,
        val autographed: Boolean? = null,
        val memorabilia: Boolean? = null,
        val issuesPerYear: String? = null,
        val itemPartNumber: String? = null,
        val label: String? = null,
        val contentLanguages: List<ContentLanguage>? = null,
        val legalDisclaimer: String? = null,
        val manafacturer: String? = null,
        val manufacturerMaximumAge: AlgopixUnitType? = null,
        val manufacturerMinimumAge: AlgopixUnitType? = null,
        val manufacturerPartsWarrantyDescription: String? = null,
        val materialType: List<String>? = null,
        val maximumResolution: AlgopixUnitType? = null,
        val metalStamp: String? = null,
        val metalType: String? = null,
        val model: String? = null,
        val numberOfDiscs: Long? = null,
        val numberOfIssues: Long? = null,
        val numberOfItems: Long? = null,
        val numberOfPages: Long? = null,
        val numberOfTracks: Long? = null,
        val operatingSystems: List<String>? = null,
        val opticalZoom: AlgopixUnitType? = null,
        val partNumber: String? = null,
        val pegiRating: String? = null,
        val platform: List<String>? = null,
        val processorCount: Long? = null,
        val publicationDate: String? = null,
        val publisher: String? = null,
        val releaseDate: String? = null,
        val ringSize: String? = null,
        val runningTime: AlgopixUnitType? = null,
        val shaftMaterial: String? = null,
        val scent: String? = null,
        val seasonSequence: String? = null,
        val seikodoProductCode: String? = null,
        val size: String? = null,
        val sizePerPearl: String? = null,
        val studio: String? = null,
        val subscriptionLength: AlgopixUnitType? = null,
        val systemMemorySize: String? = null,
        val theatricalReleaseDate: String? = null,
        val totalDiamondWeight: AlgopixUnitType? = null,
        val totalGemWeight: AlgopixUnitType? = null,
        val warranty: String? = null,
        val lang: String? = null
)

data class ContentLanguage(
        val languageName: String? = null,
        val type: String? = null
)

data class Contributor(
        val name: String? = null,
        val role: String? = null
)

data class AlgopixUnitType(
        val value: Double,
        val unitType: String
) {
    override fun toString(): String = "$value $unitType"
}

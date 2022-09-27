package io.meorg.code.view.models.product

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Product(
    @Id
    val id: String = "",

    val sourceBarCode: Set<Pair<String, TypeBarCode>> = emptySet(),
    @Indexed(unique = true, sparse = true)
    val barCode: Set<Long>? = getValidBarCodes(sourceBarCode).let { it.ifEmpty { null } },
    @Indexed
    val mpn: String?,
    val sku: String?,

    @Indexed
    val brand: String,
    @Indexed
    val model: String?,
    @Indexed
    val name: String,
    val originalName: String? = null,
//        @Indexed(unique = true)
    val slug: String = name.toSlug(),//todo: больше параметров формирования slug
    val description: String,
    val image: String?,
    val images: Set<String> = emptySet(),
    val cdnImage: String? = null,
    val cdnImages: Set<String>? = null,

    val attributes: Map<String, Map<String, Any>> = emptyMap(),
    val categories: Map<String, String>? = null,
    val originalCategories: List<String>? = null,

    val aggregationRating: AggregationRating = AggregationRating(0.0, 0),
    val totalReviews: Int? = null,
    val associateId: AssociateId? = null,

    val evaluation: Float = 0.0F,
    val ecoProduct: Boolean = false,
    val clicks: Long? = null,
    val activations: Long? = null,

    /**
     * Admin
     */
    @Indexed
    val adminMpn: String? = null,
    @Indexed
    val adminSku: String? = null,
    @Indexed
    val adminBrand: String? = null,
    @Indexed
    val adminModel: String? = null,
    @Indexed
    val adminName: String? = null,
    val adminDescription: String? = null,
    val adminImage: String? = null,
    val adminImages: Set<String>? = null,
    val adminAttributes: Map<String, Map<String, Any>>? = null,
    val adminCategories: Map<String, String>? = null,
    val adminAssociateId: AssociateId? = null,
    val adminEcoProduct: Boolean? = null,
    val adminClicks: Long? = null,
) {
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var modified: Instant? = Instant.now()

    @JsonIgnore
    @Transient
    val mpnResolved = adminMpn ?: mpn

    @JsonIgnore
    @Transient
    val skuResolved = adminSku ?: sku

    @JsonIgnore
    @Transient
    val brandResolved = adminBrand ?: brand

    @JsonIgnore
    @Transient
    val modelResolved = adminModel ?: model

    @JsonIgnore
    @Transient
    val nameResolved = adminName ?: name

    @JsonIgnore
    @Transient
    val descriptionResolved = adminDescription ?: description

    @JsonIgnore
    @Transient
    val imageResolved = adminImage ?: cdnImage ?: image

    @JsonIgnore
    @Transient
    val imagesResolved = adminImages ?: cdnImages ?: images

    @JsonIgnore
    @Transient
    val attributesResolved = adminAttributes ?: attributes

    @JsonIgnore
    @Transient
    val categoriesResolved = adminCategories ?: categories

    @JsonIgnore
    @Transient
    val associateIdResolved = adminAssociateId ?: associateId

    @JsonIgnore
    @Transient
    val ecoProductResolved = adminEcoProduct ?: ecoProduct

    @JsonIgnore
    @Transient
    val clicksResolved = adminClicks ?: clicks
}

data class AggregationRating(val value: Double? = null, val total: Int? = null)

data class AssociateId(val affiliateProductId: Set<String> = emptySet(), val localId: Set<String> = emptySet())

data class ProductRating(
    val totalRatings: Int = 0,
    val totalReviews: Int = 0,
    val rating: Double = 0.0
)

fun String.toSlug() = toLowerCase()
    .replace("\n", " ")
    .replace("[^a-z\\d\\s]".toRegex(), " ")
    .split(" ")
    .joinToString("-")
    .replace("-+".toRegex(), "-")

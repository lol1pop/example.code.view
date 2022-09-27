package io.meorg.code.view.models.product

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

@Index(searchProductIndices)
data class SearchProduct(
    @Id
    val id: String,
    val productSlug: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val image: String? = null,
    val categories: Map<String, String>? = null,
    val name: String? = null,
    val attributes: Map<String, Map<String, Any>> = emptyMap(),
    val rating: Double? = null,
    val totalReviews: Int? = null,
    val totalRatings: Int? = null,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val bestSale: Double? = null,
    val bestSalePercent: Double? = null,
    val cashback: CashBack? = null,
    val cashbackFixed: CashBack? = null,
    val totalOffers: Int? = null,
    val currency: String? = null,
    val merchants: List<String> = emptyList(),
    val region: List<String> = emptyList(),
    val active: Boolean = true,
    val attributesCount: Int? = attributes.flatMap { it.value.keys }.count(),
    val ecoProduct: Boolean = false,
    val clicks: Long? = 0L,
    val activations: Long? = 0L
) {
    @GraphQLIgnore
    @JsonIgnore
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var modified: Instant? = Instant.now()
}

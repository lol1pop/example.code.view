package io.meorg.code.view.models.product.dto

import com.expediagroup.graphql.annotations.GraphQLName
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.Id

@GraphQLName("SearchProduct")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SearchProductDTO(
    @Id
    val id: String? = null,
    val productSlug: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val image: String? = null,
    val categories: Map<String, CategoryDTO>? = null,
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
    val region: List<String> = emptyList(),
    val active: Boolean = true,
    val ecoProduct: Boolean = false
)

fun SearchProduct.toDTO(categoryNames: Map<String, String?> = emptyMap()) =
    SearchProductDTO(
        id = id,
        productSlug = productSlug,
        brand = brand,
        model = model,
        image = image,
        categories = categories
            ?.map { (key, value) -> key to CategoryDTO(value, categoryNames[value] ?: "") }
            ?.toMap(),
        name = name,
        attributes = attributes,
        rating = rating?.setScale(1),
        totalReviews = totalReviews,
        totalRatings = totalRatings,
        priceMin = priceMin,
        priceMax = priceMax,
        bestSale = bestSale.takeIf { it != null && priceMin != null && it < priceMin },
        bestSalePercent = bestSalePercent,
        cashback = cashback,
        cashbackFixed = cashbackFixed,
        totalOffers = totalOffers,
        currency = currency,
        region = region,
        active = active,
        ecoProduct = ecoProduct
    )

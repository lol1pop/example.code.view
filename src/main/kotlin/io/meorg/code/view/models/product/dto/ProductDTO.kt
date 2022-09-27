package io.meorg.code.view.models.product.dto

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.expediagroup.graphql.annotations.GraphQLName
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@GraphQLName("Product")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductDTO(
    val id: String,
    val slug: String? = null,
    val name: String,
    val brand: String,
    val model: String? = null,
    val barCode: Map<String, TypeBarCode> = emptyMap(),
    val mpn: String? = null,
    val sku: String? = null,
    val NBarCode: Long? = null,
    val description: String? = null,
    val image: String? = null,
    val images: List<String> = emptyList(),
    val categories: Map<String, CategoryDTO>? = null,
    val attributes: Map<String, Map<String, Any>>? = null,
    val aggregationRating: AggregationRating? = null,
    val totalRatings: Int? = null,
    val totalReviews: Int? = null,
    val rating: Double? = null,
    val offers: List<ProductOfferDTO>? = null,
    val associateLocalId: List<String>? = null,
    val ecoProduct: Boolean = false,
) {
    @delegate:GraphQLIgnore
    @delegate:JsonIgnore
    val mainCategories by lazy { categories?.map { it.key to it.value.id }?.toMap() }
}

fun ProductDTO.toMain() = Product(
    id = id,
    slug = slug ?: name.toSlug(),
    name = name,
    brand = brand,
    model = model,
    sourceBarCode = barCode.map { it.toPair() }.toSet(),
    mpn = mpn,
    sku = sku,
    description = description ?: "",
    image = image,
    images = images.toSet(),
    attributes = attributes ?: emptyMap(),
    categories = mainCategories,
    aggregationRating = aggregationRating ?: AggregationRating(),
    associateId = AssociateId(associateLocalId?.toSet() ?: emptySet()),
    ecoProduct = ecoProduct
)

fun Product.toDTO(
    offers: List<ProductOfferDTO> = emptyList(),
    productRating: ProductRating,
    categoryNames: Map<String, String?> = emptyMap()
) = ProductDTO(
    id = id,
    slug = slug,
    name = nameResolved,
    brand = brandResolved,
    model = modelResolved,
    barCode = sourceBarCode.toMap(),
    NBarCode = barCode?.firstOrNull(),
    mpn = mpnResolved,
    description = descriptionResolved,
    image = imageResolved ?: imagesResolved.firstOrNull() ?: "",
    images = imagesResolved.toList(),
    attributes = attributesResolved,
    categories = categoriesResolved
        ?.map { (key, value) -> key to CategoryDTO(value, categoryNames[value] ?: "") }
        ?.toMap(),
    offers = offers,
    totalReviews = productRating.totalReviews,
    totalRatings = productRating.totalRatings,
    rating = productRating.rating.setScale(1),
    aggregationRating = aggregationRating,
    associateLocalId = associateIdResolved?.localId?.toList(),
    ecoProduct = ecoProductResolved,
)

fun Product.toUpdated(dto: ProductDTO) = copy(
    brand = dto.brand,
    model = dto.model,
    categories = dto.mainCategories ?: categoriesResolved,
)

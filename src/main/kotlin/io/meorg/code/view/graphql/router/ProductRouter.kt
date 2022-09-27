package io.meorg.code.view.graphql.router

import com.expediagroup.graphql.spring.operations.Mutation
import com.expediagroup.graphql.spring.operations.Query
import com.switips.us.marketplace.unit.core.service.RecentlyViewedService
import com.switips.us.marketplace.unit.core.service.product.*
import com.switips.us.marketplace.unit.core.utils.defaultPageable
import com.switips.us.marketplace.unit.core.utils.withRoot
import com.switips.us.marketplace.unit.cqs.ProductSearchQuery
import com.switips.us.marketplace.unit.security.RequestGraphQLContext
import org.springframework.stereotype.Component

@Component
class ProductQuery(
    private val productService: ProductService,
    private val productSearchService: ProductSearchService,
    private val recentlyViewedService: RecentlyViewedService,
    private val popularProductsService: PopularProductsService,
    private val popularProductsForCategoryService: PopularProductsForCategoryService,
    private val hotProductsService: HotProductsService,
) : Query {

    @Deprecated("Now params filling from admin", ReplaceWith("Use `popularProductsSlider`"))
    suspend fun popularProducts(
        categories: List<String>,
        excludeCategories: List<String>? = null,
        size: Int? = null,
        onlyWithImages: Boolean? = null,
    ) = productSearchService.getPopularSplitByCategories(
        categories,
        excludeCategories,
        onlyWithImages ?: false,
        size ?: 100
    )

    @Deprecated("Now params filling from admin", ReplaceWith("Use `popularProductsSlider`"))
    suspend fun popularProductsPageable(
        categories: List<String>? = null,
        excludeCategories: List<String>? = null,
        size: Int? = null,
        page: Int? = null,
        onlyWithImages: Boolean? = null,
    ) = productSearchService.getPopular(
        categories,
        excludeCategories,
        onlyWithImages ?: false,
        defaultPageable(page, size, defaultSize = 100)
    )

    @Deprecated("Now params filling from admin", ReplaceWith("Use `hotProductsSlider`"))
    suspend fun hotProducts(
        page: Int? = null,
        size: Int? = null,
        category: String? = null,
        excludeCategories: List<String>? = null,
        onlyWithImages: Boolean? = null,
    ) = productSearchService.getHot(
        category,
        excludeCategories ?: emptyList(),
        onlyWithImages ?: false,
        defaultPageable(page, size, defaultSize = 100)
    )

    suspend fun popularProductsSlider(take: Int? = null) = popularProductsService.getDTO(take)

    suspend fun popularProductsForCategorySlider(
        category: String,
        take: Int? = null,
    ) = popularProductsForCategoryService.getDTO(category, take)

    suspend fun hotProductsSlider(take: Int? = null) = hotProductsService.getDTO(take)

    suspend fun searchProduct(id: String) = productSearchService.getSearchById(id)

    suspend fun searchProductByIds(ids: List<String>) = productSearchService.getSearchByIds(ids)

    suspend fun searchProducts(query: ProductSearchQuery? = null) =
        productSearchService.searchByQuery(
            if (query?.active != null) query
            else query?.copy(active = true) ?: ProductSearchQuery(active = true)
        )

    suspend fun searchProductByMerchantId(
        merchantId: String,
        query: ProductSearchQuery,
    ) = productSearchService.searchProductByMerchantId(merchantId, query.copy(active = query.active ?: true))


    suspend fun product(context: RequestGraphQLContext, id: String, onlyActiveOffers: Boolean? = null) =
        productSearchService.getDTOById(id, onlyActiveOffers ?: true)?.also { product ->
            productService.addClick(product.id)
            if (context.isAuthorizedUser)
                recentlyViewedService.addViewedProduct(context.user.id, product.id)
        }

    suspend fun products(
        page: Int? = null,
        size: Int? = null,
        onlyActiveOffers: Boolean? = null,
    ) = productSearchService.findAllDTO(defaultPageable(page, size), onlyActiveOffers ?: true)

    suspend fun ecoProduct(
        page: Int? = null,
        size: Int? = null,
        onlyActiveOffers: Boolean? = null,
    ) = productSearchService.findAllEcoProductsDTO(defaultPageable(page, size), onlyActiveOffers ?: true)

}

@Component
class ProductMutation(
    private val productService: ProductService,
) : Mutation {

    suspend fun updateNoCategoryProducts(context: RequestGraphQLContext) =
        withRoot(context) { productService.updateNoCategories().let { "ok" } }

}

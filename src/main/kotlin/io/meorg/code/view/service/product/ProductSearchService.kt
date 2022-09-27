package io.meorg.code.view.service.product

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.QueryBuilders.*
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptType
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.aggregations.AggregationBuilders.*
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation
import org.elasticsearch.search.aggregations.bucket.filter.Filter
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.Max
import org.elasticsearch.search.aggregations.metrics.Min
import org.elasticsearch.search.aggregations.metrics.TopHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ProductSearchService(
    private val elasticSearchClient: ElasticSearchClient,
    private val productRepository: ProductMongo,
    private val offerService: ProductOfferSearchService,
    private val productCategorySearchService: ProductCategorySearchService,
    private val objectMapper: ObjectMapper,
    private val reviewService: ReviewSearchService,
) {
    private val searchCommands = listOf(
        ::searchProductByBarCode,
        ::searchProductByMpnAndBrand,
        ::searchProductByModelAndBrand,
        ::searchProductByMpnOrModel,
        ::searchProductByNameAndBrand,
        ::searchProductByName
    )

    private suspend fun Map<String, String>?.getCategoryNames() =
        this?.values?.associate { it to productCategorySearchService.getName(it) } ?: emptyMap()

    private suspend fun Product.internalToDTO(offers: List<ProductOfferDTO> = emptyList()) =
        toDTO(offers, reviewService.getProductRating(id), categoriesResolved.getCategoryNames())

    private suspend fun SearchProduct.internalToDTO() =
        toDTO(categories.getCategoryNames())

    suspend fun searchProduct(options: SearchProductOptions): Product? {
        searchCommands.forEach { command -> command(options)?.let { return it } }
        return null
    }

    suspend fun searchProductByMpnAndBrand(options: SearchProductOptions): Product? {
        val mpn = options.mpn ?: return null
        val brand = options.brand ?: return null
        return if (options.includeAdminFields == false)
            productRepository.findByMpnAndBrand(mpn, brand).awaitFirstOrNull()
        else
            productRepository.findByMpnAndBrandAdmin(mpn, brand).awaitFirstOrNull()
    }

    suspend fun searchProductByModelAndBrand(options: SearchProductOptions): Product? {
        val model = options.model ?: return null
        val brand = options.brand ?: return null
        return if (options.includeAdminFields == false)
            productRepository.findByModelAndBrand(model, brand).awaitFirstOrNull()
        else
            productRepository.findByModelAndBrandAdmin(model, brand).awaitFirstOrNull()
    }

    suspend fun searchProductByBarCode(options: SearchProductOptions): Product? = options.barCode
        ?.let { productRepository.findTop1ByBarCodeIn(it).awaitFirstOrNull() }

    suspend fun searchProductByMpnOrModel(options: SearchProductOptions): Product? {
        val mpn = options.mpn ?: return null
        val model = options.model ?: return null
        return if (options.includeAdminFields == false)
            productRepository.findByMpnOrModel(mpn, model).awaitFirstOrNull()
        else
            productRepository.findByMpnOrModelAdmin(mpn, model).awaitFirstOrNull()
    }

    suspend fun searchProductByNameAndBrand(options: SearchProductOptions): Product? {
        val brand = options.brand ?: return null
        val name = options.name ?: return null
        return if (options.includeAdminFields == false)
            productRepository.findAllByOriginalNameAndBrand(name, brand).awaitFirstOrNull()
                ?: productRepository.findAllByNameAndBrand(name, brand).awaitFirstOrNull()
        else
            productRepository.findAllByNameAndBrandAdmin(name, brand).awaitFirstOrNull()
    }

    suspend fun searchProductByName(options: SearchProductOptions): Product? = options.name
        ?.let {
            if (options.includeAdminFields == false) productRepository.findAllByName(it).awaitFirstOrNull()
            else productRepository.findAllByNameAdmin(it).awaitFirstOrNull()
        }

    suspend fun getInternalSearchById(id: String): SearchProduct? =
        elasticSearchClient.get<SearchProduct>(id)

    suspend fun getInternalSearchByIds(ids: Collection<String>): Collection<SearchProduct> {
        val found = elasticSearchClient.get<SearchProduct>(ids).toList()
        return ids.mapNotNull { id -> found.find { it.id == id } }
    }

    suspend fun getSearchById(id: String): SearchProductDTO? =
        getInternalSearchById(id)?.internalToDTO()

    suspend fun getSearchByIds(ids: Collection<String>): List<SearchProductDTO> =
        getInternalSearchByIds(ids).map { it.internalToDTO() }

    suspend fun getById(id: String): Product? = productRepository.findById(id).awaitFirstOrNull()
    suspend fun getByIds(ids: Collection<String>): Flow<Product> = productRepository.findAllById(ids).asFlow()
    suspend fun getAll(page: Pageable = Pageable.unpaged()): List<Product> =
        productRepository.findAll()
            .let {
                if (page.isPaged) it.skip(page.offset).limitRequest(page.pageSize.toLong())
                else it
            }
            .asFlow()
            .toList()

    suspend fun get(ids: Collection<String>): List<Product> {
        val products = getByIds(ids)
        return ids.mapNotNull { products.firstOrNull { product -> product.id == it } }
    }

    suspend fun count(query: ProductSearchQuery): Long =
        elasticSearchClient.count<SearchProduct>(elasticQuery(query))

    suspend fun getDTOById(id: String, onlyActiveOffers: Boolean = false): ProductDTO? =
        getById(id)?.let { it.internalToDTO(it.getDTOOffers(onlyActiveOffers)) }

    private fun defaultScript(script: String, map: Map<String, Any>) =
        Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, script, map)

    private fun queryByName(name: String) = elasticSuggestSearch(SearchProduct::name.name, name)
    private fun queryByCategory(category: String) = multiMatchQuery(category, *categorySearchFields)
    private fun queryByCategoryIsNull() = boolQuery().also { it.mustNot(existsQuery(SearchProduct::categories.name)) }
    private fun queryByActive(active: Boolean) = termQuery(SearchProduct::active.name, active)
    private fun queryByMerchantIds(merchantIds: Collection<String>) =
        termsQuery(SearchProduct::merchants.name, merchantIds)

    private fun queryByPriceMin(min: Double) = scriptQuery(defaultScript(priceMinScript, mapOf("min" to min)))
    private fun queryByPriceMax(max: Double) = scriptQuery(defaultScript(priceMaxScript, mapOf("max" to max)))
    private fun queryOnlySale() = rangeQuery(SearchProduct::bestSalePercent.name).gt(0.0)
    private fun queryOnlyEcoProduct() = termQuery(SearchProduct::ecoProduct.name, true)
    private fun queryByBrands(brands: Collection<String>) = brands.map { matchQuery(SearchProduct::brand.name, it) }
    private fun queryWithImage() = existsQuery(SearchProduct::image.name)
    private fun queryIncludeCategories(categories: Collection<String>) = boolQuery().also {
        categories.forEach { c -> it.should(queryByCategory(c)) }
    }

    private fun queryExcludeCategories(categories: Collection<String>) = boolQuery().also {
        categories.forEach { c -> it.must(queryByCategory(c)) }
    }

    private fun SearchResponse?.toSearchProducts(): Pair<Long, List<SearchProduct>> =
        this?.hits.toSearchProducts()

    private fun SearchHits?.toSearchProducts(): Pair<Long, List<SearchProduct>> {
        val total = this?.totalHits?.value ?: 0L
        val data = this?.hits?.asSequence()
            ?.map { it.sourceAsMap + (SearchProduct::id.name to it.id) }
            ?.map { objectMapper.writeValueAsString(it) }
            ?.map { objectMapper.readValue(it, SearchProduct::class.java) }
            ?.toList()
            ?: emptyList()
        return total to data
    }

    private fun Collection<MultiBucketsAggregation.Bucket>.toMap() =
        mapNotNull { bucket ->
            if (bucket.keyAsString.isBlank()) null
            else bucket.keyAsString to bucket.docCount
        }.toMap()

    private fun SearchResponse?.split(field: String, size: Int? = null): Pair<Long, List<SearchProduct>> {
        var total = 0L
        return this?.aggregations
            ?.get<Terms>(field)
            ?.buckets
            ?.flatMap {
                it.aggregations.get<TopHits>(topHitAgg).hits.toSearchProducts()
                    .also { (_total, _) -> total += _total }
                    .second
            }
            ?.let {
                if (size != null && size > 0) it.take(size)
                else it
            }
            ?.shuffled()
            ?.let { total to it }
            ?: (total to emptyList())
    }

    private fun SearchResponse?.splitByCategory(size: Int? = null): Pair<Long, List<SearchProduct>> =
        split(categoryAggField, size)

    private fun SearchResponse?.splitByMerchants(size: Int? = null): Pair<Long, List<SearchProduct>> =
        split(SearchProduct::merchants.name, size)

    private fun SearchResponse?.getBrands() = this?.aggregations
        ?.get<Terms>(SearchProductResponse::brands.name)
        ?.buckets
        ?.toMap()

    private suspend fun SearchResponse?.getCategories(categoryLevels: Int) = categoryAggregationFields
        .take(categoryLevels)
        .flatMapIndexed { index, _ ->
            this?.aggregations
                ?.get<Terms>(SearchProductResponse::categories.name + "${index + 1}")
                ?.buckets
                ?: emptyList()
        }
        .toMap()
        .let { productCategorySearchService.getHierarchyByIds(it) }

    private fun SearchResponse?.getOnlySaleCounter() = this?.aggregations
        ?.get<Filter>(SearchProductResponse::onlySaleCounter.name)
        ?.docCount

    private fun SearchResponse?.getPriceMax() = this?.aggregations
        ?.get<Max>(SearchProductResponse::priceMax.name)
        ?.value
        ?.setScale()

    private fun SearchResponse?.getPriceMin() = this?.aggregations
        ?.get<Min>(SearchProductResponse::priceMin.name)
        ?.value
        ?.setScale()

    private fun SearchSourceBuilder.splitAggregation(
        field: String,
        sizePerBucket: Int,
        include: Collection<String>? = null,
        exclude: Collection<String>? = null,
        addKeyword: Boolean = true,
    ) =
        aggregation(
            terms(field)
                .field(if (addKeyword) field.keyword() else field)
                .let {
                    if (include != null || exclude != null)
                        it.includeExclude(
                            IncludeExclude(
                                include?.toTypedArray(),
                                exclude?.toTypedArray()
                            )
                        )
                    else it
                }
                .subAggregation(
                    topHits(topHitAgg).let {
                        if (sizePerBucket < 0) it.size(maxBucketSize)
                        else it.size(sizePerBucket)
                    }
                )
        )

    private fun SearchSourceBuilder.splitByMerchantAggregation(sizePerBucket: Int, include: Collection<String>?) =
        splitAggregation(SearchProduct::merchants.name, sizePerBucket, include)

    private fun SearchSourceBuilder.splitByCategoryAggregation(sizePerBucket: Int) =
        splitAggregation(categoryAggField, sizePerBucket)

    private fun SearchSourceBuilder.brandsAggregation() =
        aggregation(
            terms(SearchProductResponse::brands.name)
                .field(SearchProduct::brand.keyword())
                .size(maxBucketSize)
        )

    private fun SearchSourceBuilder.categoriesAggregation(categoryLevels: Int) =
        categoryAggregationFields
            .take(categoryLevels)
            .forEachIndexed { index, field ->
                aggregation(
                    terms(SearchProductResponse::categories.name + "${index + 1}")
                        .field(field)
                        .size(maxBucketSize)
                )
            }

    private fun SearchSourceBuilder.onlySaleCounterAggregation() =
        aggregation(filter(SearchProductResponse::onlySaleCounter.name, queryOnlySale()))

    private fun SearchSourceBuilder.priceMinMaxAggregation() {
        aggregation(
            max(SearchProductResponse::priceMax.name)
                .field(SearchProduct::priceMin.name)
        )
        aggregation(
            min(SearchProductResponse::priceMin.name)
                .field(SearchProduct::priceMin.name)
        )
    }

    @Deprecated("Old logic", ReplaceWith("Use `searchByQuery`"))
    suspend fun getPopular(
        categories: Collection<String>? = null,
        excludeCategories: Collection<String>? = null,
        onlyWithImages: Boolean = false,
        page: Pageable? = null,
    ): GraphQlPageableSearchProduct =
        elasticSearchClient.get<SearchProduct, Pair<Long?, Collection<SearchProduct>?>>(
            SearchSourceBuilder.searchSource()
                .query(
                    if (excludeCategories.isNullOrEmpty() && categories.isNullOrEmpty() && !onlyWithImages)
                        queryByActive(true)
                    else boolQuery().also { bool ->
                        if (!categories.isNullOrEmpty())
                            bool.must(queryIncludeCategories(categories))
                        if (!excludeCategories.isNullOrEmpty())
                            bool.mustNot(queryExcludeCategories(excludeCategories))
                        if (onlyWithImages)
                            bool.must(queryWithImage())
                        bool.must(queryByActive(true))
                    }
                )
                .let { SortProducts.POPULARITY.sort(it) }
                .let {
                    if (page == null || page.isUnpaged) it
                    else it.from(page.offset.toInt()).size(page.pageSize)
                }
        ) { response: SearchResponse -> response.toSearchProducts() }
            .let {
                GraphQlPageable.of(
                    total = it?.first ?: 0L,
                    pageable = page ?: Pageable.unpaged(),
                    data = it?.second?.map { product -> product.internalToDTO() } ?: emptyList()
                )
            }

    @Deprecated("Old logic", ReplaceWith("Use `searchByQuery`"))
    suspend fun getPopularSplitByCategories(
        categories: Collection<String>,
        excludeCategories: Collection<String>? = null,
        onlyWithImages: Boolean = false,
        size: Int = 100,
    ): List<SearchProductDTO>? {
        if (categories.isNullOrEmpty()) throw IllegalStateException("categories aren't specified")
        val sizePerCategory = (size / categories.count()) + 1
        return elasticSearchClient.get<SearchProduct, Collection<SearchProduct>>(
            SearchSourceBuilder.searchSource()
                .aggregation(
                    terms(SearchProduct::categories.name)
                        .field("${SearchProduct::categories.name}.1".keyword())
                        .subAggregation(topHits("docs").size(sizePerCategory))
                )
                .query(
                    if (excludeCategories.isNullOrEmpty() && categories.isNullOrEmpty() && !onlyWithImages)
                        queryByActive(true)
                    else boolQuery().also { bool ->
                        if (!categories.isNullOrEmpty())
                            bool.must(queryIncludeCategories(categories))
                        if (!excludeCategories.isNullOrEmpty())
                            bool.mustNot(queryExcludeCategories(excludeCategories))
                        if (onlyWithImages)
                            bool.must(queryWithImage())
                        bool.must(queryByActive(true))
                    }
                )
                .let { SortProducts.POPULARITY.sort(it) }
                .size(0)
        ) { response: SearchResponse ->
            response.aggregations
                .get<Terms>(SearchProduct::categories.name)
                .buckets
                .flatMap { it.aggregations.get<TopHits>("docs").hits.toSearchProducts().second }
                .let {
                    if (size > 0) it.take(size)
                    else it
                }
                .shuffled()
        }
            ?.map { it.internalToDTO() }
    }

    @Deprecated("Old logic", ReplaceWith("Use `searchByQuery`"))
    suspend fun getHot(
        category: String? = null,
        excludeCategories: Collection<String>? = null,
        onlyWithImages: Boolean = false,
        pageable: Pageable? = null,
    ): List<SearchProductDTO>? =
        elasticSearchClient.get<SearchProduct, Collection<SearchProduct>>(
            SearchSourceBuilder.searchSource()
                .query(
                    if (excludeCategories.isNullOrEmpty() && category == null && !onlyWithImages)
                        queryByActive(true)
                    else boolQuery().also { bool ->
                        if (category != null)
                            bool.must(queryByCategory(category))
                        if (!excludeCategories.isNullOrEmpty())
                            bool.mustNot(queryExcludeCategories(excludeCategories))
                        if (onlyWithImages)
                            bool.must(queryWithImage())
                        bool.must(queryByActive(true))
                    }
                )
                .sort(SearchProduct::bestSalePercent.name, SortOrder.DESC)
                .from(pageable?.offset?.toInt() ?: 0)
                .size(pageable?.pageSize ?: 100)
        ) { response: SearchResponse -> response.toSearchProducts().second }
            ?.map { it.internalToDTO() }

    private fun elasticQuery(query: ProductSearchQuery) =
        if (query.isNull()) matchAllQuery()
        else {
            boolQuery().apply {
                if (!query.search.isNullOrBlank())
                    must(queryByName(query.search))
                if (!query.resolvedCategories.isNullOrEmpty())
                    must(queryIncludeCategories(query.resolvedCategories))
                if (!query.excludeCategories.isNullOrEmpty())
                    mustNot(queryExcludeCategories(query.excludeCategories))
                if (query.active != null)
                    must(queryByActive(query.active))
                if (query.priceMin != null)
                    must(queryByPriceMin(query.priceMin))
                if (query.priceMax != null)
                    must(queryByPriceMax(query.priceMax))
                if (query.onlySale == true)
                    must(queryOnlySale())
                if (query.onlyEco == true)
                    must(queryOnlyEcoProduct())
                if (query.onlyWithImage == true)
                    must(queryWithImage())
                if (!query.brands.isNullOrEmpty())
                    must(
                        boolQuery().also { bool ->
                            queryByBrands(query.brands).forEach { bool.should(it) }
                        }
                    )
                if (!query.merchantIds.isNullOrEmpty())
                    must(queryByMerchantIds(query.merchantIds))
                if (!query.excludeMerchantIds.isNullOrEmpty())
                    mustNot(queryByMerchantIds(query.excludeMerchantIds))
                if (query.onlyWithoutCategory == true && query.category == null)
                    must(queryByCategoryIsNull())
            }
        }

    private fun SearchSourceBuilder.aggregations(query: ProductSearchQuery) =
        apply {
            if (query.includeBrands == true) brandsAggregation()
            if (query.includeCategories == true) categoriesAggregation(query.categoryLevels)
            if (query.includeOnlySaleCounter == true) onlySaleCounterAggregation()
            if (query.includePriceMinMax == true) priceMinMaxAggregation()
            when {
                query.splitByMerchantEnabled -> splitByMerchantAggregation(query.sizePerMerchant, query.merchantIds)
                query.splitByCategoryEnabled -> splitByCategoryAggregation(query.sizePerCategory)
            }
        }

    suspend fun search(query: ProductSearchQuery): SearchProductResponse =
        elasticSearchClient.get<SearchProduct, SearchResponse>(
            SearchSourceBuilder.searchSource()
                .query(elasticQuery(query))
                .aggregations(query)
                .let { (query.sort ?: SortProducts.NONE).sort(it) }
                .also {
                    when {
                        query.splitByMerchantEnabled || query.splitByCategoryEnabled -> it.size(0)
                        query.page.isPaged -> it.from(query.page.offset.toInt()).size(query.page.pageSize)
                    }
                }
        ) { response: SearchResponse -> response }
            .let { response ->
                val (total, data) =
                    when {
                        query.splitByMerchantEnabled -> response.splitByMerchants(query.pageSize)
                        query.splitByCategoryEnabled -> response.splitByCategory(query.pageSize)
                        else -> response.toSearchProducts()
                    }

                SearchProductResponse(
                    total = total,
                    data = data,
                    brands = if (query.includeBrands == true) response.getBrands() else null,
                    categories = if (query.includeCategories == true) response.getCategories(query.categoryLevels) else null,
                    onlySaleCounter = if (query.includeOnlySaleCounter == true) response.getOnlySaleCounter() else null,
                    priceMax = if (query.includePriceMinMax == true) response.getPriceMax() else null,
                    priceMin = if (query.includePriceMinMax == true) response.getPriceMin() else null,
                )
            }

    suspend fun searchByQuery(query: ProductSearchQuery): GraphQlPageableSearchProduct = search(query)
        .let {
            GraphQlPageableSearchProduct(
                it.total,
                if (query.page.isPaged) query.page.pageSize.toLong() else -1L,
                if (query.page.isPaged) query.page.pageNumber.toLong() else -1L,
                it.data.map { search -> search.internalToDTO() }
            ).apply {
                brands = it.brands
                categories = it.categories
                onlySaleCounter = it.onlySaleCounter
                priceMax = it.priceMax
                priceMin = it.priceMin
            }
        }

    suspend fun searchProductByMerchantId(
        merchantId: String,
        query: ProductSearchQuery,
    ): GraphQlPageableSearchProduct? {
//        val merchant = merchantSearchService.getById(merchantId) ?: return null
        //todo поправить с айдишника мерча на айдишник авидейтке и в searchproduct модельке тоже
        return searchByQuery(query.copy(merchantIds = listOf(merchantId)))
    }

    private suspend fun Product.getDTOOffers(onlyActive: Boolean = true) =
        offerService.searchDTOByQuery(
            ProductOfferSearchQuery(
                productId = id,
                statuses = if (onlyActive) listOf(Status.ACTIVE) else null,
                onlyActiveMerchants = true
            )
        ).data

    fun findAll(page: Pageable? = null): Flux<Product> =
        if (page != null) productRepository.findAll().skip(page.offset).limitRequest(page.pageSize.toLong())
        else productRepository.findAll()

    suspend fun findAllDTO(page: Pageable, onlyActiveOffers: Boolean = true): GraphQlPageableProduct =
        GraphQlPageable.of(
            total = productRepository.count().awaitSingle(),
            pageable = page,
            data = findAll(page).asFlow().map { it.internalToDTO(it.getDTOOffers(onlyActiveOffers)) }.toList()
        )

    fun findAllEcoProducts(page: Pageable): Flux<Product> = productRepository.findAllByEcoProduct(true)
        .skip(page.offset).limitRequest(page.pageSize.toLong())

    suspend fun findAllEcoProductsDTO(page: Pageable, onlyActiveOffers: Boolean = true): GraphQlPageableProduct =
        GraphQlPageable.of(
            total = productRepository.countByEcoProduct(true).awaitSingle(),
            pageable = page,
            data = findAllEcoProducts(page).asFlow().map { it.internalToDTO(it.getDTOOffers(onlyActiveOffers)) }
                .toList()
        )

    suspend fun exists(id: String) = productRepository.existsById(id).awaitFirstOrDefault(false)
    suspend fun existsSearch(id: String) = elasticSearchClient.exists<SearchProduct>(id)

    companion object {
        private val categoryAggField = "${SearchProduct::categories.name}.1"
        private const val topHitAgg = "docs"

        private val categorySearchFields by lazy {
            List(MAX_CATEGORY_LEVEL) { id -> SearchProduct::categories.name + ".${id + 1}" }.toTypedArray()
        }

        private val categoryAggregationFields by lazy {
            List(MAX_CATEGORY_LEVEL) { id -> "${SearchProduct::categories.name}.${id + 1}".keyword() }
        }

        private val priceMinScript =
            """
                if (doc['${SearchProduct::bestSale.name}'].size() != 0 && doc['${SearchProduct::bestSale.name}'].value != 0) doc['${SearchProduct::bestSale.name}'].value >= params.min;
                else 
                    if (doc['${SearchProduct::priceMin.name}'].size() != 0 && doc['${SearchProduct::priceMin.name}'].value != 0) doc['${SearchProduct::priceMin.name}'].value >= params.min;
                    else false;
            """.trimIndent()

        private val priceMaxScript =
            """
                if (doc['${SearchProduct::bestSale.name}'].size() != 0 && doc['${SearchProduct::bestSale.name}'].value != 0) doc['${SearchProduct::bestSale.name}'].value <= params.max;
                else 
                    if (doc['${SearchProduct::priceMin.name}'].size() != 0 && doc['${SearchProduct::priceMin.name}'].value != 0) doc['${SearchProduct::priceMin.name}'].value <= params.max;
                    else false;
            """.trimIndent()

        private const val maxBucketSize = 6000
    }
}

data class SearchProductOptions(
    val barCode: Set<Long>? = null,
    val brand: String? = null,
    val mpn: String? = null,
    val model: String? = null,
    val name: String? = null,
    val includeAdminFields: Boolean? = null,
)

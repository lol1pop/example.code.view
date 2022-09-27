package io.meorg.code.view.service.product

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.stream.consumeAsFlow
import mu.KotlinLogging
import org.elasticsearch.index.query.QueryBuilders.termQuery
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val elasticSearchClient: ElasticSearchClient,
    private val productMongo: ProductMongo,
    private val productCategoryService: ProductCategoryService,
    private val productCategorySearchService: ProductCategorySearchService,
    private val reviewService: ReviewSearchService,
    private val rakutenProductCategoryService: RakutenProductCategoryService,
    private val productOfferSearchService: ProductOfferSearchService,
    private val suggestService: SuggestService,
    private val imageService: ImageService,
    private val objectMapper: ObjectMapper,
) {

    private suspend fun getById(id: String): Product? = productMongo.findById(id).awaitFirstOrNull()

    private suspend fun updateCDNImage(id: String, image: String? = null, images: Collection<String>? = null) {
        if (image != null || images != null) {
            updateRelated(id, true)
        }
    }

    private suspend fun uploadImages(product: Product) {
        val image = product.cdnImage
            ?: product.image?.let { img -> imageService.justUpload(img, product.id, UploadType.PRODUCT) }
        val images = product.cdnImages
            ?: imageService.justUpload(product.images.toTypedArray(), product.id, UploadType.PRODUCT)
        updateCDNImage(product.id, image, images)
    }

    private suspend fun saveSearch(product: SearchProduct) {
        elasticSearchClient.save(product)
        suggestService.saveProductOrDelete(product)
    }

    suspend fun createSearchProduct(id: String): Boolean = getById(id)?.let { createSearchProduct(it) } ?: false
    suspend fun createSearchProduct(product: Product): Boolean =
        createSearchProduct(product, productOfferSearchService.findActiveByProductId(product.id).toList())

    suspend fun createSearchProduct(id: String, offers: List<ProductOffer>): Boolean =
        getById(id)?.let { createSearchProduct(it, offers) } ?: false

    suspend fun createSearchProduct(product: Product, offers: List<ProductOffer>): Boolean =
        try {
            val activeOffers = offers.filter { it.isRealActive }.filter { it.priceResolved.currency == productCurrency }
            val reviewInfo = reviewService.getProductRating(product.id)
            val priceMin = activeOffers.minOfOrNull { it.priceResolved.amount }
            val priceMax = activeOffers.maxOfOrNull { it.priceResolved.amount }
            val bestSale = activeOffers.minOfOrNull { it.priceResolved.sale ?: it.priceResolved.amount }
            val bestSalePercent = activeOffers.maxOfOrNull { it.priceResolved.salePercent ?: 0.0 }
            val cashBack = activeOffers.maxByOrNull { it.cashBackResolved.raw }?.cashBackResolved
            val cashbackFixed =
                if (priceMin != null && cashBack != null) CashBack.of(priceMin * cashBack.raw / 100) else null
            val merchants = activeOffers.map { it.merchantId }.distinct()
            val searchProduct =
                SearchProduct(
                    id = product.id,
                    productSlug = product.slug,
                    brand = product.brandResolved,
                    model = product.modelResolved,
                    image = product.imageResolved,
                    categories = product.categoriesResolved,
                    name = product.nameResolved,
                    attributes = product.attributesResolved.filterKeys { it.isNotBlank() },
                    priceMin = priceMin,
                    priceMax = priceMax,
                    bestSale = bestSale,
                    bestSalePercent = bestSalePercent,
                    cashback = cashBack,
                    cashbackFixed = cashbackFixed,
                    totalOffers = activeOffers.size,
                    currency = productCurrency,
                    merchants = merchants,
                    rating = reviewInfo.rating,
                    totalReviews = reviewInfo.totalReviews,
                    totalRatings = reviewInfo.totalRatings,
                    ecoProduct = product.ecoProductResolved,
                    clicks = product.clicksResolved,
                    activations = product.activations,
                    active = activeOffers.isNotEmpty()
                )
            saveSearch(searchProduct)
            true
        } catch (e: Exception) {
            logger.error { "[X] product id:${product.id} - ${product.nameResolved} do not created SearchProduct e:$e" }
            false
        }

    /**
     * Минус этой функции в том, что если у оффера изменилась цена - и она была, допустим минимальна,
     * То получить реальную минимальную цену - не представляется возможным
     * Поэтому использовать ТОЛЬКО при добавлении нового оффера (как сейчас это и делается)
     */
    suspend fun addProductOffer(productOffer: ProductOffer) =
        productOffer.isRealActive.takeIf { it }?.let {
            val cashback = productOffer.cashBackResolved
            val cashbackFixed = CashBack.of(productOffer.priceResolved.amount * productOffer.cashBackResolved.raw / 100)
            elasticSearchClient.updateScript<SearchProduct>(
                productOffer.productId,
                addProductOfferScript,
                mapOf(
                    "price" to productOffer.priceResolved.amount,
                    "bestSale" to (productOffer.priceResolved.sale ?: productOffer.priceResolved.amount),
                    "bestSalePercent" to (productOffer.priceResolved.salePercent ?: 0.0),
                    "cashback" to objectMapper.convertValue(cashback, Map::class.java),
                    "cashbackFixed" to objectMapper.convertValue(cashbackFixed, Map::class.java),
                    "merchant" to productOffer.merchantId
                )
            ) == productOffer.productId
        }
            ?.also { suggestService.enableProduct(productOffer.productId) }
            ?: true

    suspend fun save(product: Product, createEmptySearch: Boolean = false) =
        productMongo.save(product).awaitSingle().also { p ->
            p.categories?.values?.distinct()?.let { productCategoryService.incProductCount(it) }
            createSearchProduct(p, emptyList())
        }

    suspend fun updateNoCategories() {
        var count = 0
        logger.info { "Init updateNoCategories" }
        productMongo.findAllWithNoCategories().asFlow().collect { product ->
            let {
                product.originalCategories?.firstOrNull()?.let { productCategorySearchService.getByRawId(it) }
                    ?: rakutenProductCategoryService.googleCategoryId(product.originalCategories)
            }
                ?.let {
                    count++
                    if (count % 1000 == 0 && count > 0) {
                        logger.info { "$count - updateNoCategories" }
                    }
                }
        }
        logger.info { "Finished updateNoCategories - $count" }
    }

    suspend fun updateCategoryCounts() {
        var count = 0L
        logger.info { "Init update category product count" }
        productCategoryService.dropAllProductCount()
        elasticSearchClient.all<SearchProduct>(termQuery(SearchProduct::active.name, true)).consumeAsFlow()
            .collect { product ->
                if (product.categories.isNullOrEmpty()) return@collect
                productCategoryService.incProductCount(product.categories.values)
                count++
                if (count % 1000L == 0L) logger.info { "Update category product count progress: $count" }
            }
        logger.info { "Finish Update category product count (count: $count)" }
    }

    suspend fun addClick(id: String) {
        elasticSearchClient.softInc<SearchProduct>(id, SearchProduct::clicks.name)
    }

    suspend fun addActivation(id: String) {
        elasticSearchClient.softInc<SearchProduct>(id, SearchProduct::activations.name)
    }

    suspend fun updateName(id: String, name: String) =
                elasticSearchClient.softUpdate<SearchProduct>(id, mapOf(SearchProduct::name.name to name)) == id &&
                suggestService.updateProduct(id, name)

    suspend fun updateRelated(
        id: String,
        updatedNameOrImage: Boolean = false,
        categories: Map<String, String>? = null,
        prevCategories: Map<String, String>? = null,
    ) {
        /**
         * Update search product & suggest & category counters
         */
        if (updatedNameOrImage || categories != null) createSearchProduct(id)
        categories?.values?.let { cs -> productCategoryService.incProductCount(cs) }
        prevCategories?.values?.let { cs -> productCategoryService.decProductCount(cs) }
    }

    suspend fun uploadImagesToCDN(ids: Collection<String>? = null) {
        var count = 0L
        logger.info { "Init product upload images to CDN (ids: $ids)" }
        let {
            if (ids != null) productMongo.findAllById(ids)
            else productMongo.findAll()
        }.collect { product ->
            if (count++ % 500 == 0L) logger.info { "Progress product upload images to CDN: $count" }
            uploadImages(product)
        }
        logger.info { "Finish product upload images to CDN (total: $count)" }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
        private const val productCurrency = "USD"
        private val addProductOfferScript =
            """
                if (ctx._source.${SearchProduct::priceMin.name} == null || ctx._source.${SearchProduct::priceMin.name} > params.price) {
                    ctx._source.${SearchProduct::priceMin.name} = params.price;
                }
                if (ctx._source.${SearchProduct::priceMax.name} == null || ctx._source.${SearchProduct::priceMax.name} < params.price) {
                    ctx._source.${SearchProduct::priceMax.name} = params.price;
                }
                if (ctx._source.${SearchProduct::bestSale.name} == null || ctx._source.${SearchProduct::bestSale.name} > params.bestSale) {
                    ctx._source.${SearchProduct::bestSale.name} = params.bestSale;
                    ctx._source.${SearchProduct::bestSalePercent.name} = params.bestSalePercent;
                }
                if (ctx._source.${SearchProduct::cashback.name} == null || ctx._source.${
                SearchProduct::cashback.nested(
                    CashBack::classic
                )
            } < params.cashback.${CashBack::classic.name}) {
                    ctx._source.${SearchProduct::cashback.name} = params.cashback;
                }
                if (ctx._source.${SearchProduct::cashbackFixed.name} == null || ctx._source.${
                SearchProduct::cashbackFixed.nested(
                    CashBack::classic
                )
            } < params.cashbackFixed.${CashBack::classic.name}) {
                    ctx._source.${SearchProduct::cashbackFixed.name} = params.cashbackFixed;
                }
                if (!ctx._source.${SearchProduct::merchants.name}.contains(params.merchant)) { 
                    ctx._source.${SearchProduct::merchants.name}.add(params.merchant); 
                }
                if (ctx._source.${SearchProduct::totalOffers.name} == null) {
                    ctx._source.${SearchProduct::totalOffers.name} = 1;
                }
                else {
                    ctx._source.${SearchProduct::totalOffers.name} = ctx._source.${SearchProduct::totalOffers.name} + 1;
                }
                ctx._source.${SearchProduct::active.name} = true;
            """.trimIndent()
    }
}

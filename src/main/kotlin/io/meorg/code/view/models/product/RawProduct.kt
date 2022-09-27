package io.meorg.code.view.models.product

import java.time.Instant
import java.time.LocalDateTime

data class RawProduct(
    val extId: String,
    val barCode: Set<Pair<String, TypeBarCode>> = emptySet(),   //todo: upc, gtin, isnb, ean
    val mpn: String? = null,
    val sku: String? = null,
    val name: String? = null,
    val originalName: String? = name,
    val brand: String? = null,
    val model: String? = null,
    val description: String? = null,
    val attributes: Map<String, Any> = emptyMap(),  //todo: gender, color, size, material  ...
    val country: String? = null,              //todo: где находится товар
    val language: String? = null,
    val geo: Geo? = null,
    val categories: Map<String, String>? = null,
    val originalCategories: List<String>? = null,
    val image: String? = null,
    val pictures: List<String> = emptyList(),
    val manufacturer: String? = null,
    val shopId: String? = null,
    val shopName: String? = null,
    val currency: String? = null,
    val price: Double,
    val salePrice: Double? = null,
    val discount: Double? = null,
    val commission: Set<Commission> = emptySet(),
    val unitMeasure: String? = null,
    val unitPricingMeasure: String? = null,
    val delivery: Boolean = false,
    val availability: String? = null,
    val availabilityDate: LocalDateTime? = null,
    val serviceableAreas: Set<String> = emptySet(),
    val shipping: Shipping = Shipping(),
    val link: String? = null,
    val activateLink: String? = null,
    val domain: String? = null,
    val advertiserId: String,
    val advertiserName: String?,
    val advertiserLogo: String? = null,
    val mongoMerchantId: String,
    val externalMerchantId: String,
    val affiliateType: AffiliateEnum = AffiliateEnum.NONE,
    val sourceType: SourceType = SourceType.NONE,
    val itemListId: List<String> = emptyList(),  //todo: то что подходит для этого товара типо как наушники или чехол для телефона, джостик для приставки
    val assocProductId: List<String> = emptyList(),
    val custom: Map<String, Any> = emptyMap(),
    val ecoProduct: Boolean = false,
    val ecoParams: Map<String, Any> = emptyMap(),
    val lastUpdated: Instant?
) {
    val sale by lazy { salePrice?.let { if (it >= price) null else salePrice } }
    val priceModel by lazy { Price.of(price, sale, currency) }
    val cashback by lazy { CashBackMerchant.of(commission) }
    val activationLink by lazy {
        when {
            !activateLink.isNullOrBlank() -> activateLink
            affiliateType != AffiliateEnum.NONE -> "" //todo: генерить линку
            else -> ""
        }
    }
    val hash by lazy {
        calculationHashProductOffer(
            price = priceModel,
            cashback = cashback.max.raw,
            productId = extId,
            merchantId = externalMerchantId,
            originalActivationLink = activationLink
        )
    }
}

data class Shipping(
    val country: List<String> = emptyList(),
    val countryCode: String? = null,
    val height: String? = null,
    val length: String? = null,
    val locationGroupName: String? = null,
    val locationId: String? = null,
    val postalCode: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val localCost: Double? = null,
    val taxShipping: Double? = null,
    val region: String? = null,
    val service: String? = null,
    val weight: String? = null,
    val width: String? = null
)

data class SemanticFields(
    val associateSem3Id: Set<String> = emptySet()
)

/*
 * cashback/discount - это значения на конкретный оффер
 * commission - это значения кэшбека или скидки на магазин в целом
 */

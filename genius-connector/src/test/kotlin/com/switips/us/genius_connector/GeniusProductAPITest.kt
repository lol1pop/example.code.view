package com.switips.us.genius_connector

import com.switips.us.genius_connector.response.product.GeniusProduct
import com.switips.us.genius_connector.response.product.GeniusProductItem
import com.switips.us.genius_connector.response.product.GeniusProductPricing
import org.junit.Assert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

/**
 * Для проверки корректности схемы реального сервиса 
 * Genius необходим рабочий API Token
 * 
 * Поскольку бесплатный токен имеет лимит 100 запросов в месяц
 * Поэтому тесты с реальным сервисом Genius
 * Выключены и запускаются только если указан API Token
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GeniusProductAPITest {

    private val api by lazy { GeniusProductAPI.provider() }
    private val apiKey by lazy {
        System.getenv(SYSTEM_ENV_API_KEY)
                ?: throw IllegalArgumentException("No api token specified")
    }

    private val mock by lazy { MockServer.createMock() }
    private val mockApi by lazy { GeniusProductAPI.provider(mock.url("/").toString()) }
    private val mockApiKey by lazy { "mock_api_token" }

    @BeforeAll
    fun setUp() {
        mock.start()
    }

    @AfterAll
    fun endUp() {
        mock.close()
    }

    @Test
    fun mockIdentifiers() {
        mockApi.identifiers(mockApiKey, upc = testUpc).get().let { model -> Assert.assertEquals(identifiersModel, model) }
    }

    @Test
    fun mockLookup() {
        mockApi.lookup(mockApiKey, upc = testUpc).get().let { model -> Assert.assertEquals(lookupModel, model) }
    }

    @Test
    fun mockProductData() {
        mockApi.productData(mockApiKey).get().let { model -> Assert.assertEquals(productDataModel, model) }
    }

    @Test
    fun mockSearch() {
        mockApi.search(mockApiKey, keyword = testKeyword).get().let { model -> Assert.assertEquals(searchModel, model) }
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = SYSTEM_ENV_API_KEY, matches = NOT_EMPTY_ENV_REGEX)
    fun identifiers() {
        api.identifiers(apiKey, upc = testUpc).get().let { model -> Assert.assertEquals(identifiersModel, model) }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = SYSTEM_ENV_API_KEY, matches = NOT_EMPTY_ENV_REGEX)
    fun lookup() {
        api.lookup(apiKey, upc = testUpc).get().let { model -> Assert.assertEquals(lookupModel, model) }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = SYSTEM_ENV_API_KEY, matches = NOT_EMPTY_ENV_REGEX)
    fun productData() {
        api.productData(apiKey).get().let { model -> Assert.assertEquals(productDataModel, model) }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = SYSTEM_ENV_API_KEY, matches = NOT_EMPTY_ENV_REGEX)
    fun search() {
        api.search(apiKey, keyword = testKeyword).get().let { model -> Assert.assertEquals(searchModel, model) }
    }

    companion object {
        private const val SYSTEM_ENV_API_KEY = "GENIUS_PRODUCT_API_KEY"
        private const val NOT_EMPTY_ENV_REGEX = ".+"

        private val testUpc by lazy { "074676621708" }
        private val testKeyword by lazy { "Mueller Adjust-To-Fit Elbow Support" }
        
        private val identifiersModel by lazy {
            GeniusProduct(
                    success = true,
                    status = 200,
                    identifier = "074676621708",
                    identifierType = "upc",
                    items = GeniusProductItem(
                            title = "Mueller Adjust-To-Fit Elbow Support",
                            asin = "B019FWQ1IS",
                            upc = "074676621708",
                            ean = "0074676621708",
                            mpn = "6217",
                            brand = "Mueller",
                            ebayId = "163789977527"
                    )
            )
        }

        private val lookupModel by lazy {
            GeniusProduct(
                    success = true,
                    status = 200,
                    identifier = "074676621708",
                    identifierType = "upc",
                    items = GeniusProductItem(
                            ean = "0074676621708",
                            title = "Mueller Adjust-To-Fit Elbow Support",
                            description = "Mueller Adjust-To-Fit Elbow Support Gender: unisex. Age Group: adult.",
                            upc = "074676621708",
                            brand = "Mueller",
                            mpn = "6217",
                            color = "Gray",
                            size = "",
                            dimension = "",
                            weight = "",
                            category = "Health & Beauty > Health Care > Supports & Braces",
                            currency = "",
                            lowestPrice = 5.24,
                            highestPrice = 42.08,
                            images = listOf(
                                    "https://i5.walmartimages.com/asr/5d0659f7-6ff9-4679-9dc2-8c90d0dcb9e1_1.b709a949fb1451d03f629ffff15606a5.jpeg?odnHeight=450&odnWidth=450&odnBg=ffffff",
                                    "https://target.scene7.com/is/image/Target/GUEST_c0adb49d-b02b-4e3c-b193-54ae83d74ee2?wid=1000&hei=1000",
                                    "http://ecx.images-amazon.com/images/I/51p6FyjHL6L._SL160_.jpg"
                            ),
                            pricing = listOf(
                                    GeniusProductPricing(
                                            seller = "Wal-Mart.com",
                                            websiteName = "walmart.com",
                                            title = "Mueller Adjust-to-Fit Elbow Support, Gray, One Size Fits Most",
                                            currency = "",
                                            price = 8.47,
                                            shipping = "5.99",
                                            condition = "New",
                                            link = "https://www.walmart.com/ip/Mueller-Adjust-to-Fit-Elbow-Support-Gray-One-Size-Fits-Most/131247616",
                                            dateFound = 1591169530L
                                    ),
                                    GeniusProductPricing(
                                            seller = "Amazon.com",
                                            websiteName = "amazon.com",
                                            title = "Mueller Sports Medicine Adjust-to-Fit Elbow Support, 0.29 Pound",
                                            currency = "",
                                            price = 14.99,
                                            shipping = "Free Shipping",
                                            condition = "New",
                                            link = "http://www.amazon.com/Mueller-Sports-Medicine-Adjust-Support/dp/B019FWQ1IS",
                                            dateFound = 1457628939L
                                    ),
                                    GeniusProductPricing(
                                            seller = "Target",
                                            websiteName = "target.com",
                                            title = "Mueller Adjust-To-Fit Elbow Support",
                                            currency = "",
                                            price = 12.79,
                                            shipping = "",
                                            condition = "New",
                                            link = "https://www.target.com/p/mueller-adjust-to-fit-elbow-support/-/A-17089051&intsrc=CATF_1444",
                                            dateFound = 1598410781L
                                    )
                            ),
                            asin = "B019FWQ1IS",
                            ebayId = "163789977527"
                    )
            )
        }

        private val productDataModel by lazy {
            GeniusProduct(
                    success = false,
                    status = 404,
                    message = "No products found for this identifier."
            )
        }
        
        private val searchModel by lazy {
            GeniusProduct(
                    success = true,
                    status = 200,
                    items = GeniusProductItem(
                            ean = "0074676673318",
                            title = "Mueller Tennis Elbow Support",
                            description = "Get through your day with the support you need from the Mueller Tennis Elbow Support. This arm band can be adjusted to fit most any size arm and provides a secure fit so that you can stay in motion. Gender: unisex. Age Group: adult.",
                            upc = "074676673318",
                            brand = "Mueller",
                            mpn = "074676673318",
                            color = "Stainless steel",
                            size = "",
                            dimension = "",
                            weight = "1.00lb",
                            category = "Health & Beauty > Health Care > Supports & Braces",
                            currency = "",
                            lowestPrice = 5.0,
                            highestPrice = 19.99,
                            images = listOf(
                                    "https://pics.drugstore.com/prodimg/521984/450.jpg",
                                    "https://i5.walmartimages.com/asr/3524399c-1d31-4eb8-a79c-ccc270c7c675_1.667aeebb5db23f38b9ec2802d5f0caa9.jpeg?odnHeight=450&odnWidth=450&odnBg=ffffff",
                                    "https://target.scene7.com/is/image/Target/GUEST_bbb90acd-fc64-4e61-83ad-a8ad97b882f2?wid=1000&hei=1000",
                                    "http://pics1.ds-static.com/prodimg/521984/300.jpg",
                                    "http://ecx.images-amazon.com/images/I/31-%2BEP8FH1L._SL160_.jpg"
                            ),
                            pricing = listOf(
                                    GeniusProductPricing(
                                            seller = "Walgreens",
                                            websiteName = "walgreens.com",
                                            title = "Mueller Sport Care Tennis Elbow Support, Maximum Support, Model 6733 One Size - 1.0 ea",
                                            currency = "",
                                            price = 19.99,
                                            shipping = "US:::5.49 USD",
                                            condition = "New",
                                            link = "https://www.walgreens.com/store/c/mueller-sport-care-tennis-elbow-support,-maximum-support,-model-6733-one-size/ID=prod6212269-product",
                                            dateFound = 1599215036L
                                    ),
                                    GeniusProductPricing(
                                            seller = "Drugstore",
                                            websiteName = "drugstore.com",
                                            title = "Mueller Tennis Elbow Support, Black, One Size, 1 ea",
                                            currency = "",
                                            price = 14.44,
                                            shipping = "5.99",
                                            condition = "New",
                                            link = "http://www.drugstore.com/products/prod.asp?pid=521984&catid=60001",
                                            dateFound = 1482545242L
                                    ),
                                    GeniusProductPricing(
                                            seller = "Wal-Mart.com",
                                            websiteName = "walmart.com",
                                            title = "Adjust-To-Fit® Tennis Elbow Support",
                                            currency = "",
                                            price = 8.99,
                                            shipping = "5.99",
                                            condition = "New",
                                            link = "https://www.walmart.com/ip/Adjust-To-Fit-Tennis-Elbow-Support/151906854",
                                            dateFound = 1591167806L
                                    ),
                                    GeniusProductPricing(
                                            seller = "Amazon Marketplace New",
                                            websiteName = "amazon.com",
                                            title = "Mueller Tennis Elbow Support, 1 ea",
                                            currency = "",
                                            price = 7.99,
                                            shipping = "",
                                            condition = "New",
                                            link = "http://www.amazon.com/gp/offer-listing/B00JPE3V30",
                                            dateFound = 1473885390L
                                    ),
                                    GeniusProductPricing(
                                            seller = "Target",
                                            websiteName = "target.com",
                                            title = "Mueller Tennis Elbow Support",
                                            currency = "",
                                            price = 12.99,
                                            shipping = "",
                                            condition = "New",
                                            link = "https://www.target.com/p/mueller-tennis-elbow-support/-/A-17089050&intsrc=CATF_1444",
                                            dateFound = 1598410781L
                                    )
                            ),
                            asin = "B00JPE3V30",
                            ebayId = "383710716502"
                    )
            )
        }        
    }
}
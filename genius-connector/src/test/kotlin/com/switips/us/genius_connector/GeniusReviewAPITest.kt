package com.switips.us.genius_connector

import com.switips.us.genius_connector.response.review.GeniusReview
import com.switips.us.genius_connector.response.review.GeniusReviewItem
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
 * Поскольку бесплатный токен имеет лимит 25 запросов в месяц
 * Поэтому тесты с реальным сервисом Genius
 * Выключены и запускаются только если указан API Token
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GeniusReviewAPITest {

    private val api by lazy { GeniusReviewAPI.provider() }
    private val apiKey by lazy {
        System.getenv(SYSTEM_ENV_API_KEY)
                ?: throw IllegalArgumentException("No api token specified")
    }

    private val mock by lazy { MockServer.createMock() }
    private val mockApi by lazy { GeniusReviewAPI.provider(mock.url("/").toString()) }
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
    fun mockReviews() {
        mockApi.reviews(mockApiKey, testQuery).get().let { model -> Assert.assertEquals(reviewsModel, model) }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = SYSTEM_ENV_API_KEY, matches = NOT_EMPTY_ENV_REGEX)
    fun reviews() {
        api.reviews(apiKey, testQuery).get().let { model -> Assert.assertEquals(reviewsModel, model) }
    }

    companion object {
        private const val SYSTEM_ENV_API_KEY = "GENIUS_REVIEW_API_KEY"
        private const val NOT_EMPTY_ENV_REGEX = ".+"

        private val testQuery by lazy { "playstation 5" }

        private val reviewsModel by lazy {
            GeniusReview(
                    success = true,
                    status = 200,
                    title = "Details about Sony Playstation 5 - Disc Edition",
                    sku = "",
                    brand = "Ubisoft",
                    reviews = listOf(
                            GeniusReviewItem(
                                    rating = 4.2,
                                    reviewAuthor = "rhko-26",
                                    link = "https://www.ebay.com/itm/PS5-PlayStation-5-Console-Disc-Edition-PREORDER/193669096461?hash=item2d1793e40d:g:B5kAAOSwFQhfYsrj",
                                    reviewTitle = "\$1k too less for this. Awesome Product",
                                    reviewDescription = "Die hard fan of PS5. Even \$1k is less for this.",
                                    reviewPublishDate = "2020-09-17"
                            )
                    )
            )
        }
    }
}
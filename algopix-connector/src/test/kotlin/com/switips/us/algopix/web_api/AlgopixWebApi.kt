package com.switips.us.algopix.web_api

import com.switips.us.algopix.AlgopixWebProductApi
import com.switips.us.algopix.Markets
import com.switips.us.algopix.response.product.data.Image
import com.switips.us.algopix.response.product.data.ImageDimension
import com.switips.us.algopix.response.product.data.ImageDimensionUnit
import com.switips.us.algopix.response.product.identification.*
import com.switips.us.algopix.response.webinterface.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import java.io.IOException
import kotlin.jvm.Throws

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AlgopixWebApi {
    private val mockWebServer = MockWebServer()
    private val mockedApi = AlgopixWebProductApi.provider(mockWebServer.url("/").toString())

    @AfterAll
    @Throws(IOException::class)
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun search() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testResponseBody))
        val response = mockedApi.identify(bearer = testBearer, query = testUpc).get()
        Assertions.assertEquals(response, testResponseObject)
    }

    companion object {
        private val testBearer = "Bearer valid token"
        private val testUpc = "887276410401"
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        private val testResponseBody = this::class.java.classLoader.getResource("search.json").readText()
        private val testResponseObject = AlgopixWebResponse(
                requestId = "0187fc0b-69c0-4805-9613-30235f3f96a7",
                statusCode = 200000,
                errorDetails = "",
                data = ItemResult(
                        marketItem = AlgopixDataItem(
                                allIds = AlgopixBarCodes(
                                        AID = setOf("AIDYKDY62001"),
                                        EAN = setOf("0887276410401"),
                                        UPC = setOf("887276410401"),
                                        ASIN = setOf("B086Z2XFYP"),
                                        IDENTIFIED_KEYWORDS = setOf("Samsung Galaxy Tab S6 Lite")
                                ),
                                name = "Samsung Galaxy Tab S6 Lite 10.4\", 64GB WiFi Tablet Oxford Gray - SM-P610NZAAXAR - S Pen Included",
                                description = "Samsung Galaxy Tab S6 Lite 10.4\", 64GB WiFi Tablet Oxford Gray - SM-P610NZAAXAR - S Pen Included",
                                model = "SM-P610NZAAXAR",
                                brand = "Samsung Electronics",
                                color = "Oxford Gray",
                                size = "10.4",
                                manufacturer = "Samsung",
                                dimensions = Dimensions(
                                        width = Length(6.07, LengthUnit.INCH),
                                        length = Length(0.28, LengthUnit.INCH),
                                        height = Length(9.63, LengthUnit.INCH)
                                ),
                                packageDimensions = Dimensions(
                                        width = Length(6.54, LengthUnit.INCH),
                                        length = Length(10.16, LengthUnit.INCH),
                                        height = Length(1.5, LengthUnit.INCH),
                                        weight = Weight(24.8, WeightUnit.OUNCE)
                                ),
                                largeImageUrl = "https://m.media-amazon.com/images/I/418Ty89Cf3L.jpg",
                                identifierIndication = "ID",
                                algopixImagesSet = setOf(Image(
                                        imageType = "large",
                                        imageUrl = "https://m.media-amazon.com/images/I/418Ty89Cf3L.jpg",
                                        imageHeight = ImageDimension(61.00, ImageDimensionUnit.PIXELS),
                                        imageWidth = ImageDimension(75.00, ImageDimensionUnit.PIXELS)
                                )),
                                algopixValidatedAttributes = WebAttributes(
                                        partNumber = "SM-P610NZAAXAR",
                                        releaseDate = "2020-05-29",
                                        cpuManufacturer = "Fujitsu",
                                        cpuSpeed = AlgopixUnitType(2.3, "GHz"),
                                        displaySize = AlgopixUnitType(10.4, ""),
                                        hardDiskSize = AlgopixUnitType(64.0, "GB"),
                                        systemMemorySize = AlgopixUnitType(64.0, "GB"),
                                        hardwarePlatform = "Android",
                                        label = "Samsung",
                                        numberOfItems = 1,
                                        operatingSystems = listOf("Android"),
                                        publisher = "Samsung",
                                        studio = "Samsung",
                                        title = "Samsung Galaxy Tab S6 Lite 10.4\", 64GB WiFi Tablet Oxford Gray - SM-P610NZAAXAR - S Pen Included",
                                        warranty = "1 Year Manufacturer",
                                        lang = "en-US"
                                ),
                                market = Markets.AMAZON_US.name
                        ),
                        originalQuery = "887276410401"
                )
        )
    }
}

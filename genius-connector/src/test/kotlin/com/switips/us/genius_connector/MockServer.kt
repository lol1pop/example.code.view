package com.switips.us.genius_connector

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

internal object MockServer {

    private fun loadFromResources(endPoint: String) =
            this::class.java.classLoader.getResource(
                    endPoint.substringAfterLast("/").substringBefore("?") + ".json"
            )?.readText()

    fun createMock() =
            MockWebServer().apply {
                requireClientAuth()
                dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                            request.path?.toLowerCase()?.let { path ->
                                loadFromResources(path)?.let { body -> MockResponse().setResponseCode(200).setBody(body) }
                            } ?: MockResponse().setResponseCode(404)

                }
            }

}
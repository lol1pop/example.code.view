package com.switips.us.genius_connector.response.review

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeniusReviewItem(
        val rating: Double? = null,
        val link: String? = null,
        @JsonProperty("review_author")
        val reviewAuthor: String? = null,
        @JsonProperty("review_title")
        val reviewTitle: String? = null,
        @JsonProperty("review_description")
        val reviewDescription: String? = null,
        @JsonProperty("review_publish_date")
        val reviewPublishDate: String? = null
) {
    @delegate:JsonIgnore
    val reviewPublishLocalDate by lazy { reviewPublishDate?.let { LocalDate.parse(it) } ?: LocalDate.EPOCH }
}
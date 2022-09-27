package com.switips.us.algopix.response.product

data class AlgopixResponse<T>(
        val timestamp: Long,
        val status: AlgopixResponseStatus,
        val statusMessage: String,
        val statusCode: Long,
        val requestId: String,
        val result: T? = null
)

enum class AlgopixResponseStatus {
    SUCCESS,
    ERROR,
    WARNING
}

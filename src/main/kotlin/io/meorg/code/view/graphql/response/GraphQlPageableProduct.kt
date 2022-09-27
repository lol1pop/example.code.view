package io.meorg.code.view.graphql.response

data class GraphQlPageableProduct(
        override val totalCount: Long,
        override val pageSize: Long,
        override val page: Long,
        val data: List<ProductDTO> = emptyList()
) : GraphQlPageable

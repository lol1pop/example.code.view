package io.meorg.code.view.graphql.response;

import org.springframework.data.domain.Pageable

/**
 * Вынесено в интерфейс
 * Генерик сделать не получается, потому что тогда ругается GraphQL
 * На то, что не умеет работать с классом T
 */
interface GraphQlPageable {
    val totalCount: Long
    val pageSize: Long
    val page: Long
    val hasNextPage get() = totalCount > pageSize * (page + 1);

    companion object {
        inline fun <reified T: GraphQlPageable, D> of(
                total: Long = 0L,
                pageable: Pageable = Pageable.unpaged(),
                data: List<D> = emptyList(),
                apply: T.() -> Unit = { }
        ): T {
            val (pageSize, page) =
            if (pageable.isUnpaged) -1L to -1L
                    else pageable.pageSize.toLong() to pageable.pageNumber.toLong()

            return T::class.java
                    .getConstructor(Long::class.java, Long::class.java, Long::class.java, List::class.java)
                    .newInstance(total, pageSize, page, data)
                    .apply { apply() }
        }
    }
}

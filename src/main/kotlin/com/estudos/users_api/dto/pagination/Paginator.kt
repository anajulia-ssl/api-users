package com.estudos.users_api.dto.pagination

import com.estudos.users_api.dto.PageQuery
import org.springframework.data.domain.Page
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import kotlin.math.max

object Paginator {

    fun <T : Any, R : Any> build(
        query: PageQuery,
        page: Page<T>,
        mapper: (T) -> R
    ): PageResponse<R> {

        val limit = page.size
        val offset = query.offset
        val total = page.totalElements
        val size = page.numberOfElements

        val links = createLinks(offset, limit, total)

        val result = PageResult(
            limit = limit,
            offset = offset,
            total = total,
            size = size
        )

        return PageResponse(
            resultSet = result,
            items = page.content.map(mapper),
            links = links
        )
    }

    /* ------------------ helpers ------------------- */

    private fun createLinks(offset: Int, limit: Int, total: Long): PageLinks {
        val base = ServletUriComponentsBuilder.fromCurrentRequest()

        val first = link(base.cloneBuilder(), 0, limit)
        val self  = link(base.cloneBuilder(), offset, limit)

        val lastOffset = if (total <= 0) 0 else ((total - 1) / limit).toInt() * limit
        val last  = link(base.cloneBuilder(), lastOffset, limit)

        val prev  = if (offset > 0) link(base.cloneBuilder(), max(0, offset - limit), limit) else null
        val next  = if (offset + limit < total) link(base.cloneBuilder(), offset + limit, limit) else null

        return PageLinks(first = first, self = self, last = last, prev = prev, next = next)
    }

    private fun link(builder: UriComponentsBuilder, offset: Int, limit: Int): PageLink =
        PageLink(
            builder
                .replaceQueryParam("offset", offset)
                .replaceQueryParam("limit", limit)
                .build(true)
                .toUriString()
        )
}
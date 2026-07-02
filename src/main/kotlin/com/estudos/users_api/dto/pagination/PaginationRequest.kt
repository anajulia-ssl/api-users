package com.estudos.users_api.dto.pagination

import com.estudos.users_api.annotation.Sortable
import com.estudos.users_api.enums.SortDirection
import com.estudos.users_api.exception.InvalidPaginationException
import com.estudos.users_api.exception.InvalidSortException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

const val MAX_PAGE_LIMIT = 100

data class PageQuery(
    val offset: Int = 0,
    val limit: Int = 20,
    val sort: String? = null
)

private fun <T : Any> getAliases(entityClass: KClass<T>): Map<String, String> =
    entityClass.memberProperties.mapNotNull { prop ->
        prop.annotations.filterIsInstance<Sortable>().firstOrNull()?.let { ann ->
            val external = ann.external.ifBlank { prop.name }
            external to prop.name
        }
    }.toMap()

fun <T : Any> PageQuery.toPageable(entityClass: KClass<T>): Pageable {
    if (offset < 0 || limit < 1 || limit > MAX_PAGE_LIMIT) {
        throw InvalidPaginationException("Invalid pagination parameters")
    }

    val page = offset / limit
    val aliases = getAliases(entityClass)

    if (sort.isNullOrBlank()) {
        val defaultProp = aliases["name"] ?: aliases.values.firstOrNull() ?: "id"
        return PageRequest.of(page, limit, Sort.by(Sort.Order.asc(defaultProp)))
    }

    val orders = sort.split(",").map { item ->
        val parts = item.split(":", limit = 2)
        // "sort=name" (sem direção) ou "sort=:asc" (sem campo) devem virar 400, não 500.
        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw InvalidSortException("Invalid sorting parameters")
        }
        val (external, direction) = parts

        val internal = aliases[external]
            ?: throw InvalidSortException("Invalid sorting parameters")

        val dir = try {
            SortDirection.from(direction)
        } catch (e: IllegalArgumentException) {
            throw InvalidSortException("Invalid sorting parameters")
        }

        Sort.Order(dir.springDirection, internal)
    }

    return PageRequest.of(page, limit, Sort.by(orders))
}
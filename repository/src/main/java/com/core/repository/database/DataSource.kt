package com.core.repository.database

import com.core.repository.repository.DataSourceResponse

/**
 *
 * Data source interface contains most popular types of requests (to db & network)
 * @suspend - coroutines support
 *
 * @Query - object allows to create map of @property & @value which is @property & @value of Entity in db
 * @Query object using getAllAsync(Query) method for
 *
 */

interface DataSource<T : Any> {

    suspend fun getAllAsync(): DataSourceResponse<List<T>>

    suspend fun getOneAsync(): DataSourceResponse<T>

    suspend fun getAllAsync(query: Query<T>): DataSourceResponse<List<T>>

    suspend fun saveAll(list: List<T>)

    suspend fun save(item: T)

    suspend fun removeAll(list: List<T>)

    suspend fun remove(item: T)

    suspend fun query(): Query<T> {
        return Query(this)
    }

    class Query<T : Any> constructor(private val dataSource: DataSource<T>) {

        val params: MutableMap<String, String> = mutableMapOf()

        fun has(property: String): Boolean {
            return params[property] != null
        }

        fun get(property: String): String? {
            return params[property]
        }

        fun where(property: String, value: String): Query<T> {
            params[property] = value
            return this
        }

        suspend fun findAll(): DataSourceResponse<List<T>> {
            return dataSource.getAllAsync(this)
        }
    }
}
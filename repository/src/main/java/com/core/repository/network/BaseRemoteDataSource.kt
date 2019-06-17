package com.core.repository.database

import com.core.repository.repository.DataSourceResponse

/**
 *
 * May be deleted
 *
 */

open class BaseRemoteDataSource<T : Any> internal constructor() : DataSource<T> {
    override suspend fun getAllAsync(): DataSourceResponse<List<T>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getAllAsync(query: DataSource.Query<T>): DataSourceResponse<List<T>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun saveAll(list: List<T>) {
        TODO("not implemented") //To change body of created fu \danctions use File | Settings | File Templates.
    }

    override suspend fun save(item: T) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun removeAll(list: List<T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun remove(item: T) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
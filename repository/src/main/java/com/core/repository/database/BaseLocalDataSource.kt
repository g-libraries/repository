package com.core.repository.database

import androidx.room.EmptyResultSetException
import com.core.repository.repository.DataSourceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 *
 * Base Local Data Source
 * @db - dao object
 * @tablename - db table name(entity class name). For db queries
 * Implements DataSource<Entity> interface
 *
 * Contains trivial logic(convert, etc)
 *
 * Can be extended for adding custom logic for different use-cases
 *
 */

open class BaseLocalDataSource<Entity : Any, daoObject : BaseDao<Entity>> constructor(
    private val db: daoObject,
    private val tableName: String
) :
    DataSource<Entity> {

    override suspend fun saveAll(list: List<Entity>) = withContext(Dispatchers.IO) {
        db.insertAll(list)
    }

    override suspend fun save(item: Entity) = withContext(Dispatchers.IO) {
        db.insert(item)
    }

    override suspend fun removeAll(list: List<Entity>) = withContext(Dispatchers.IO) {
        db.deleteAll(list)
    }

    override suspend fun remove(item: Entity) = withContext(Dispatchers.IO) {
        db.delete(item)
    }

    override suspend fun getAllAsync(): DataSourceResponse<List<Entity>> = withContext(Dispatchers.IO) {
        val response = DataSourceResponse<List<Entity>>()

        try {
            response.successful(db.rawQuery(sqlWhere(tableName, query().params)))
        } catch (e: EmptyResultSetException) {
            response.unSuccessful(-1, e.message!!, false)
        }
    }

    override suspend fun getAllAsync(query: DataSource.Query<Entity>): DataSourceResponse<List<Entity>> =
        withContext(Dispatchers.IO) {
            val response = DataSourceResponse<List<Entity>>()

            try {
                response.successful(db.rawQuery(sqlWhere(tableName, query().params)))
            } catch (e: EmptyResultSetException) {
                response.unSuccessful(-1, e.message!!, false)
            }
        }
}
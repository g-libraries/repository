package com.core.repository.repository

import com.core.repository.database.DataSource
import com.core.repository.network.launchSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 *
 * Repository
 *
 * Basic repository logic
 * Returns @Entity or @Entities of requested @Entities
 * suspend - coroutines support
 *
 * @remoteDataSource - @DataSource<Entity> interface
 * @localDataSource - @DataSource<Entity> interface
 *
 * Connects @remoteDataSource & localDataSource
 *
 * Contains "where i need to search/save that" logic
 *
 */


open class Repository<Entity : Any>(
    private val remoteDataSource: DataSource<Entity>,
    private val localDataSource: DataSource<Entity>
) {

    suspend fun getAllAsync(): DataSourceResponse<List<Entity>> = withContext(Dispatchers.IO) {
        obtainResult(this)
    }

//    /**
//     * Сreating queries to database
//     */
//
//    suspend fun getAllAsync(query: DataSource.Query<Entity>) = withContext(Dispatchers.IO) {
//        obtainResult(this)
//    }
//
//    suspend fun saveAll(list: List<Entity>) = withContext(Dispatchers.IO) {
//        //todo cache network logic
//        localDataSource.saveAll(list)
//    }
//
//
//    suspend fun save(item: Entity) = withContext(Dispatchers.IO) {
//        //todo cache network logic
//        localDataSource.save(item)
//    }
//
//    suspend fun removeAll(list: List<Entity>) = withContext(Dispatchers.IO) {
//        //todo cache network logic
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        null
//    }
//
//    suspend fun remove(item: Entity) = withContext(Dispatchers.IO) {
//        //todo cache network logic
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        null
//    }


    fun obtainResult(scope: CoroutineScope): DataSourceResponse<List<Entity>> {
        var response = DataSourceResponse<List<Entity>>()

        fun handeDbError(throwable: Throwable) {
            response.unSuccessful(-1, "dbError", false)
        }

        //Handle response obtain error
        fun handeInternalError(throwable: Throwable) {
            scope.launchSafe(::handeDbError) {
                response = localDataSource.getAllAsync()
            }
        }

        scope.launchSafe(::handeInternalError) {
            response = remoteDataSource.getAllAsync()

            response.getResultSafe({
                scope.launchSafe(::handeDbError) {
                    localDataSource.saveAll(it)
                }
            }, {
                scope.launchSafe(::handeDbError) {
                    localDataSource.getAllAsync().getResultSafe({
                        //  Data from local data source with server error

                        response.result = it
                    }, {
                        // No data available
                        response.result = null
                    })
                }
            }, {
                scope.launchSafe(::handeDbError) {
                    response = localDataSource.getAllAsync()
                }
            }, {
                scope.launchSafe(::handeDbError) {
                    response = localDataSource.getAllAsync()
                }
            })
        }

        return response
    }

}
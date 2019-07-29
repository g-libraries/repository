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
        obtainResultList(this)
    }

    suspend fun getOneAsync(): DataSourceResponse<Entity> = withContext(Dispatchers.IO) {
        obtainResult(this)
    }

    suspend fun saveAll(list: List<Entity>) = withContext(Dispatchers.IO) {
        //todo cache network logic
        localDataSource.saveAll(list)
    }

    suspend fun save(item: Entity) = withContext(Dispatchers.IO) {
        //todo cache network logic
        localDataSource.save(item)
    }


    fun obtainResultList(scope: CoroutineScope): DataSourceResponse<List<Entity>> {
        var response = DataSourceResponse<List<Entity>>()

        fun handeDbError(throwable: Throwable) {
            response.unSuccessful(-1, "dbError : ${throwable.message}", false)
        }

        fun requestToDB() {
            scope.launchSafe(::handeDbError) {
                localDataSource.getAllAsync().getResultSafe({
                    //  Data from local data source with server error

                    response.result = it
                }, {
                    // No data available
                    response.result = null
                })
            }
        }

        //Handle response obtain error
        fun handeInternalError(throwable: Throwable) {
            response.unSuccessful(-1, "response obtain error : ${throwable.message}", false)
            requestToDB()
        }

        scope.launchSafe(::handeInternalError) {
            response = remoteDataSource.getAllAsync()

            response.getResultSafe({
                scope.launchSafe(::handeDbError) {
                    localDataSource.saveAll(it)
                }
            }, {
                requestToDB()
            }, {
                requestToDB()
            }, {
                requestToDB()
            })
        }

        return response
    }


    fun obtainResult(scope: CoroutineScope): DataSourceResponse<Entity> {
        var response = DataSourceResponse<Entity>()

        fun handeDbError(throwable: Throwable) {
            response.unSuccessful(-1, "dbError : ${throwable.message}", false)
        }

        fun requestToDB() {
            scope.launchSafe(::handeDbError) {
                localDataSource.getOneAsync().getResultSafe({
                    //  Data from local data source with server error

                    response.result = it
                }, {
                    // No data available
                    response.result = null
                })
            }
        }

        //Handle response obtain error
        fun handeInternalError(throwable: Throwable) {
            response.unSuccessful(-1, "response obtain error : ${throwable.message}", false)
            requestToDB()
        }

        scope.launchSafe(::handeInternalError) {
            response = remoteDataSource.getOneAsync()

            response.getResultSafe({
                scope.launchSafe(::handeDbError) {
                    localDataSource.save(it)
                }
            }, {
                requestToDB()
            }, {
                requestToDB()
            }, {
                requestToDB()
            })
        }

        return response
    }


}
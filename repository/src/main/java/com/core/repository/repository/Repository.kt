package com.core.repository.repository

import com.core.repository.database.DataSource
import com.core.repository.network.launchSafe
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception

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
        obtainResultList()
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


    suspend fun obtainResultList(): DataSourceResponse<List<Entity>> = runBlocking {
        var response = DataSourceResponse<List<Entity>>()

        fun handeDbError(throwable: Throwable) {
            response.unSuccessful(-1, "dbError : ${throwable.message}", false)
        }

        fun requestToDB() {
            launchSafe(::handeDbError) {
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
            response.unSuccessful(-1, "response obtain error : ${throwable.message}", true)
            requestToDB()
        }


        launchSafe(::handeInternalError) {
            response = remoteDataSource.getAllAsync()

            response.getResultSafe({
                launchSafe(::handeDbError) {
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

        response
    }

    suspend fun obtainResult(scope: CoroutineScope): DataSourceResponse<Entity> {
        var response = DataSourceResponse<Entity>()

        runBlocking {
            fun handeDbError(throwable: Throwable) {
                response.unSuccessful(-1, "dbError : ${throwable.message}", false)
            }

            fun requestToDB() {
                launchSafe(::handeDbError) {
                    try {
                        localDataSource.getOneAsync().getResultSafe({
                            //  Data from local data source with server error
                            response.result = it
                        }, {
                            // No data available
                            response.result = null
                        })
                    } catch (e: Exception) {
                        handeDbError(e)
                    }
                }
            }

            //Handle response obtain error
            fun handeInternalError(throwable: Throwable) {
                response.unSuccessful(-1, "response obtain error : ${throwable.message}", false)
                requestToDB()
            }

            try {
                response = remoteDataSource.getOneAsync()

                response.getResultSafe({
                    launchSafe(::handeDbError) {
                        try {
                            localDataSource.save(it)
                        } catch (e: Exception) {
                            handeDbError(e)
                        }
                    }
                }, {
                    requestToDB()
                }, {
                    requestToDB()
                }, {
                    requestToDB()
                })
            } catch (e: Exception) {
                handeInternalError(e)
            }
        }
        return response
    }
}
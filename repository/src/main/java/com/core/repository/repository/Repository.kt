package com.core.repository.repository

import com.core.repository.database.DataSource
import com.core.repository.network.launchSafe
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
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

@Suppress("EXPERIMENTAL_API_USAGE")
open class Repository<Entity : Any>(
    private val remoteDataSource: DataSource<Entity>,
    private val localDataSource: DataSource<Entity>
) {


    suspend fun getAllAsync(scope: CoroutineScope): ReceiveChannel<DataSourceResponse<List<Entity>>> =
        scope.produce {
            var response = DataSourceResponse<List<Entity>>()

            //Handle db error
            fun handeDbError(throwable: Throwable) {
                Timber.e(throwable)
                response.unSuccessful(-1, "Unexpected error", false)
            }

            fun handeDbSaveError(throwable: Throwable) {
                Timber.e(throwable)
            }

            //Handle response obtain error
            fun handleInternalError(throwable: Throwable) {
                Timber.e(throwable)
                response.unSuccessful(-1, "Unexpected error", true)
            }

            launchSafe(::handeDbError) {
                try {
                    this@Repository.localDataSource.getAllAsync().getResultSafe({
                        //  Data from local data source with server error
                        response.successful(it)
                    }, {
                        // No data available
                        response.unSuccessful(it.peekContent(), false)
                    })
                } catch (e: Exception) {
                    handleInternalError(e)
                }
            }.join()

            send(response)

            response = DataSourceResponse()

            launchSafe(::handleInternalError) {
                try {
                    this@Repository.remoteDataSource.getAllAsync().getResultSafe({
                        response.successful(it)
                        launchSafe(::handeDbSaveError) {
                            try {
                                this@Repository.localDataSource.saveAll(it)
                            } catch (e: Exception) {
                                handeDbSaveError(e)
                            }
                        }
                    }, {
                        response.unSuccessful(it.peekContent(), true)
                    })
                } catch (e: Exception) {
                    handleInternalError(e)
                }
            }.join()

            send(response)
        }

    suspend fun getOneAsync(scope: CoroutineScope): ReceiveChannel<DataSourceResponse<Entity>> =
        scope.produce {
            var response = DataSourceResponse<Entity>()

            //Handle db error
            fun handeDbError(throwable: Throwable) {
                Timber.e(throwable)
                response.unSuccessful(-1, "Unexpected error", false)
            }

            fun handeDbSaveError(throwable: Throwable) {
                Timber.e(throwable)
            }

            //Handle response obtain error
            fun handeInternalError(throwable: Throwable) {
                Timber.e(throwable)
                response.unSuccessful(-1, "Unexpected error", true)
            }

            launchSafe(::handeDbError) {
                try {
                    this@Repository.localDataSource.getOneAsync().getResultSafe({
                        //  Data from local data source with server error
                        response.successful(it)
                    }, {
                        // No data available
                        response.unSuccessful(it.peekContent(), true)
                    })
                } catch (e: Exception) {
                    handeDbError(e)
                }
            }.join()

            send(response)

            response = DataSourceResponse()

            launchSafe(::handeInternalError) {
                try {
                    this@Repository.remoteDataSource.getOneAsync().getResultSafe({
                        response.successful(it)
                        launchSafe(::handeDbSaveError) {
                            try {
                                this@Repository.localDataSource.save(it)
                            } catch (e: Exception) {
                                handeDbSaveError(e)
                            }
                        }
                    }, {
                        response.unSuccessful(it.peekContent(), true)
                    })
                } catch (e: Exception) {
                    handeInternalError(e)
                }
            }.join()

            send(response)
        }

    suspend fun saveAll(list: List<Entity>, remote: Boolean, local: Boolean) =
        withContext(Dispatchers.IO) {
            if (remote) {
                remoteDataSource.saveAll(list)
            }

            if (local) {
                localDataSource.saveAll(list)
            }
        }

    suspend fun delete(item: Entity, remote: Boolean, local: Boolean) =
        withContext(Dispatchers.IO) {
            if (remote) {
                remoteDataSource.remove(item)
            }

            if (local) {
                localDataSource.remove(item)
            }
        }

    suspend fun save(item: Entity, remote: Boolean, local: Boolean) = withContext(Dispatchers.IO) {
        if (remote) {
            remoteDataSource.save(item)
        }

        if (local) {
            localDataSource.save(item)
        }
    }

}
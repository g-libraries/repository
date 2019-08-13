package com.core.repository.repository

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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


open class Repository<Entity : Any>(
    private val remoteDataSource: DataSource<Entity>,
    private val localDataSource: DataSource<Entity>
) {

    suspend fun getAllAsync(): MutableLiveData<DataSourceResponse<List<Entity>>> = withContext(Dispatchers.IO) {
        var response = DataSourceResponse<List<Entity>>()

        val remoteDataSource = MutableLiveData<DataSourceResponse<List<Entity>>>()
        val localDataSource = MutableLiveData<DataSourceResponse<List<Entity>>>()

        val merger = MediatorLiveData<DataSourceResponse<List<Entity>>>()

        merger.addSource(remoteDataSource) { postedValue -> merger.postValue(postedValue) }
        merger.addSource(localDataSource) { postedValue -> merger.postValue(postedValue) }

        //Handle db error
        fun handeDbError(throwable: Throwable) {
            response.unSuccessful(-1, "dbError : ${throwable.message}", false)
            localDataSource.value = response
        }

        //Handle response obtain error
        fun handeInternalError(throwable: Throwable) {
            response.unSuccessful(-1, "response obtain error : ${throwable.message}", true)
            remoteDataSource.value = response
        }

        fun makeRequest() {
            launchSafe(::handeInternalError) {
                response = this@Repository.remoteDataSource.getAllAsync()

                response.getResultSafe({
                    localDataSource.value = response
                    merger.removeSource(localDataSource)
                    launchSafe(::handeDbError) {
                        this@Repository.localDataSource.saveAll(it)
                    }
                }, {
                    response.unSuccessful(-1, "dbError : ${it.errorMessage}", true)
                    remoteDataSource.value = response
                })
            }
        }

        launchSafe(::handeDbError) {
            this@Repository.localDataSource.getAllAsync().getResultSafe({
                //  Data from local data source with server error
                response.result = it
                localDataSource.value = response
            }, {
                // No data available
                response.unSuccessful(-1, "dbError : ${it.errorMessage}", false)
                localDataSource.value = response
            })
        }

        makeRequest()

        merger
    }

    suspend fun getOneAsync(): MutableLiveData<DataSourceResponse<Entity>> = withContext(Dispatchers.IO) {
        var response = DataSourceResponse<Entity>()

        val remoteDataSource = MutableLiveData<DataSourceResponse<Entity>>()
        val localDataSource = MutableLiveData<DataSourceResponse<Entity>>()

        val merger = MediatorLiveData<DataSourceResponse<Entity>>()

        merger.addSource(remoteDataSource) { postedValue -> merger.postValue(postedValue) }
        merger.addSource(localDataSource) { postedValue -> merger.postValue(postedValue) }

        //Handle db error
        fun handeDbError(throwable: Throwable) {
            response.unSuccessful(-1, "dbError : ${throwable.message}", false)
            localDataSource.value = response
        }

        //Handle response obtain error
        fun handeInternalError(throwable: Throwable) {
            response.unSuccessful(-1, "response obtain error : ${throwable.message}", true)
            remoteDataSource.value = response
        }

        fun makeRequest() {
            try {
                launchSafe(::handeInternalError) {
                    response = this@Repository.remoteDataSource.getOneAsync()

                    response.getResultSafe({
                        localDataSource.value = response
                        merger.removeSource(localDataSource)
                        launchSafe(::handeDbError) {
                            try {
                                this@Repository.localDataSource.save(it)
                            } catch (e: Exception) {
                                handeDbError(e)
                            }
                        }
                    }, {
                        response.unSuccessful(-1, "dbError : ${it.errorMessage}", true)
                        remoteDataSource.value = response
                    })
                }
            } catch (e: Exception) {
                handeInternalError(e)
            }
        }

        launchSafe(::handeDbError) {
            try {
                this@Repository.localDataSource.getOneAsync().getResultSafe({
                    //  Data from local data source with server error
                    response.result = it
                    localDataSource.value = response
                }, {
                    // No data available
                    response.unSuccessful(-1, "dbError : ${it.errorMessage}", false)
                    localDataSource.value = response
                })
            } catch (e: Exception) {
                handeDbError(e)
            }
        }

        makeRequest()

        merger
    }

    suspend fun saveAll(list: List<Entity>) = withContext(Dispatchers.IO) {
        //todo cache network logic
        localDataSource.saveAll(list)
    }

    suspend fun save(item: Entity) = withContext(Dispatchers.IO) {
        //todo cache network logic
        localDataSource.save(item)
    }

}
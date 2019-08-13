package com.core.repository.repository

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.core.repository.database.DataSource
import com.core.repository.network.launchSafe
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.intellij.lang.annotations.Flow
import org.w3c.dom.Entity
import timber.log.Timber
import java.lang.Exception
import java.util.function.Consumer
import java.util.function.Function

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
    suspend fun getAllAsync(): ReplaySubject<DataSourceResponse<List<Entity>>> = withContext(Dispatchers.IO) {
        val subject = ReplaySubject.create<DataSourceResponse<List<Entity>>>()
        var response = DataSourceResponse<List<Entity>>()


        //Handle db error
        fun handeDbError(throwable: Throwable) {
            response.unSuccessful(-1, "dbError : ${throwable.message}", false)
            subject.onNext(response)
        }

        //Handle response obtain error
        fun handeInternalError(throwable: Throwable) {
            response.unSuccessful(-1, "response obtain error : ${throwable.message}", true)
            subject.onNext(response)
        }

        fun makeRequest() {
            launchSafe(::handeInternalError) {
                try {
                    response = this@Repository.remoteDataSource.getAllAsync()

                    response.getResultSafe({
                        subject.onNext(response)
                        subject.onComplete()
                        launchSafe(::handeDbError) {
                            try {
                                this@Repository.localDataSource.saveAll(it)
                            } catch (e: Exception) {
                                handeDbError(e)
                            }
                        }
                    }, {
                        response.unSuccessful(-1, "dbError : ${it.errorMessage}", true)
                        subject.onNext(response)
                        subject.onComplete()
                    })
                } catch (e: Exception) {
                    handeInternalError(e)
                }
            }
        }

        launchSafe(::handeDbError) {
            try {
                this@Repository.localDataSource.getAllAsync().getResultSafe({
                    //  Data from local data source with server error
                    response.result = it
                    subject.onNext(response)
                }, {
                    // No data available
                    response.unSuccessful(-1, "dbError : ${it.errorMessage}", false)
                    subject.onNext(response)
                })
            } catch (e: Exception) {
                handeDbError(e)
            }
        }

        makeRequest()

        subject
    }

    suspend fun getOneAsync(): ReplaySubject<DataSourceResponse<Entity>> = withContext(Dispatchers.IO) {
        val subject = ReplaySubject.create<DataSourceResponse<Entity>>()
        var response = DataSourceResponse<Entity>()

        //Handle db error
        fun handeDbError(throwable: Throwable) {
            response.unSuccessful(-1, "dbError : ${throwable.message}", false)
            subject.onNext(response)
        }

        //Handle response obtain error
        fun handeInternalError(throwable: Throwable) {
            response.unSuccessful(-1, "response obtain error : ${throwable.message}", true)
            subject.onNext(response)
        }

        fun makeRequest() {
            launchSafe(::handeInternalError) {
                try {
                    response = this@Repository.remoteDataSource.getOneAsync()

                    response.getResultSafe({
                        subject.onNext(response)
                        subject.onComplete()
                        launchSafe(::handeDbError) {
                            try {
                                this@Repository.localDataSource.save(it)
                            } catch (e: Exception) {
                                handeDbError(e)
                            }
                        }
                    }, {
                        response.unSuccessful(-1, "dbError : ${it.errorMessage}", true)
                        subject.onNext(response)
                        subject.onComplete()
                    })
                } catch (e: Exception) {
                    handeInternalError(e)
                }
            }
        }

        launchSafe(::handeDbError) {
            try {
                this@Repository.localDataSource.getOneAsync().getResultSafe({
                    //  Data from local data source with server error
                    response.result = it
                    subject.onNext(response)
                }, {
                    // No data available
                    response.unSuccessful(-1, "dbError : ${it.errorMessage}", false)
                    subject.onNext(response)
                })
            } catch (e: Exception) {
                handeDbError(e)
            }
        }

        makeRequest()

        subject
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
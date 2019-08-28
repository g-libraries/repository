package com.core.repository.database

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.coroutines.Deferred

/**
 * Base database DAO
 * Basic methods which need to have every db DAO
 * Maybe created BaseDaos with different @ConflictStrategy
 * Can be extended
 */

interface BaseDao<T : Any> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(item: List<T>)

    @Delete
    suspend fun delete(item: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: T)

    @Delete
    suspend fun deleteAll(item: List<T>)

    @RawQuery
    fun rawQuery(query: SupportSQLiteQuery): List<T>

    @RawQuery
    fun rawOneQuery(query: SupportSQLiteQuery): T
}
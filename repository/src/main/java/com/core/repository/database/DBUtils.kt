package com.core.repository.database

import androidx.sqlite.db.SimpleSQLiteQuery

/**
 *  Util method for converting params map from @Query to sql QUERY with WHERE keyword
 */

fun sqlWhere(table: String, params: Map<String, String>): SimpleSQLiteQuery {
    var query = "SELECT * FROM $table"
    params.keys.forEachIndexed { i, s ->
        query += if (i == 0) " WHERE" else " AND"
        query += " $s = ?"
    }

    val args = params.values.toTypedArray()
    return SimpleSQLiteQuery(query, args)
}
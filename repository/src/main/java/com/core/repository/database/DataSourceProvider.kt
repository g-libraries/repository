package com.core.repository.database

import kotlin.reflect.KClass

/**
 *
 * Data Source Provider
 *
 * Providing DataSource<Entity> object
 * Works more like dependency injection
 *
 */

interface DataSourceProvider {
    fun <Entity : Any> of(clazz: KClass<*>): DataSource<Entity>
}
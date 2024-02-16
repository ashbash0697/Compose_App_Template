package com.example.core.utils

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val dataStoreMap = mapOf(
    0 to "old",
    1 to "new",
)

fun storeName(version: Int) = dataStoreMap[version]!!

val Context.appDataStore by preferencesDataStore(
    name = storeName(1),
    produceMigrations = { context ->
        listOf(
            context.migrateToV1
        )
    }
)

val Context.migrateToV1: DataMigrationWrapper
    get() = object : DataMigrationWrapper(
        context = this,
        version = 1
    ) {
        override fun migrate(oldData: Preferences, currentData: MutablePreferences) {
            // things to migration go here
        }
    }
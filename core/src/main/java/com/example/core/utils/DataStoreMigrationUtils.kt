package com.example.core.utils

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val versionKey = intPreferencesKey("version")

abstract class DataMigrationWrapper(
    private val context: Context,
    private val version: Int
) : DataMigration<Preferences> {

    private val Context.oldDataStore by preferencesDataStore(storeName(version - 1))

    override suspend fun cleanUp() {
        context.oldDataStore.edit { it.clear() }
    }

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return (currentData[versionKey] ?: 0) < version
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val oldData = context.oldDataStore.data.first().asMap()
        val currentMutablePrefs = currentData.toMutablePreferences()
        copyAllData(oldData, currentMutablePrefs)

        migrate(context.oldDataStore.data.first(), currentMutablePrefs)

        currentMutablePrefs[versionKey] = version
        return currentMutablePrefs
    }

    abstract fun migrate(oldData: Preferences, currentData: MutablePreferences)
}

fun copyAllData(
    oldData: Map<Preferences.Key<*>, Any>,
    currentMutablePrefs: MutablePreferences
) {
    oldData.forEach { (key, value) ->
        when (value) {
            is Boolean -> currentMutablePrefs[booleanPreferencesKey(key.name)] = value
            is String -> currentMutablePrefs[stringPreferencesKey(key.name)] = value
            is Int -> currentMutablePrefs[intPreferencesKey(key.name)] = value
            is Long -> currentMutablePrefs[longPreferencesKey(key.name)] = value
            is Double -> currentMutablePrefs[doublePreferencesKey(key.name)] = value
            is Float -> currentMutablePrefs[floatPreferencesKey(key.name)] = value
            is Set<*> -> currentMutablePrefs[stringSetPreferencesKey(key.name)] =
                value as Set<String>
        }
    }
}
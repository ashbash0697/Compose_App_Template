package com.example.core.login.datasource

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.core.utils.AESEncryption
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AuthTokenLocalDatasource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    val json: Json
) {

    suspend fun getTokenData() = tokenDataFlow.firstOrNull() ?: TokenData()

    suspend fun getAuthToken() = getTokenData().authToken

    suspend fun getRefreshToken() = getTokenData().authToken

    private val tokenDataFlow = dataStore.data
        .catch {
            emit(emptyPreferences())
        }.distinctUntilChangedBy { it[TokenData.DATA_STORE_KEY] }
        .map {
            it[TokenData.DATA_STORE_KEY]?.let { value ->
                try {
                    val storedValue = AESEncryption.decrypt(value) ?: ""
                    json.decodeFromString(TokenData.serializer(),storedValue)
                } catch (_: Exception) {
                    TokenData()
                }
            } ?: TokenData()
        }

    suspend fun updateTokens(authToken: String, refreshToken: String) {
        val token = getTokenData()
        dataStore.edit {
            json.encodeToString(
                TokenData.serializer(),
                token.copy(authToken = authToken, refreshToken = refreshToken)
            )
        }
    }

    suspend fun updateAuthTokens(authToken: String) {
        val token = getTokenData()
        dataStore.edit {
            json.encodeToString(
                TokenData.serializer(),
                token.copy(authToken = authToken)
            )
        }
    }

    suspend fun clearData() {
        dataStore.edit {
            it[TokenData.DATA_STORE_KEY] = ""
        }
    }
}

@Serializable
data class TokenData(
    val authToken: String? = null,
    val refreshToken: String? = null,
    val userId: String? = null
) {
    companion object {
        val DATA_STORE_KEY = stringPreferencesKey("TokenData")
    }
}

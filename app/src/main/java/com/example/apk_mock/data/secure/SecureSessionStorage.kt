package com.example.apk_mock.data.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.apk_mock.domain.repository.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecureSessionStorage(context: Context) {
    private val applicationContext = context.applicationContext
    private val preferencesLock = Any()

    @Volatile
    private var preferences: SharedPreferences? = null

    suspend fun saveSession(token: String, user: User) {
        withContext(Dispatchers.IO) {
            encryptedPreferences().edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, user.id)
                .putString(KEY_USER_NAME, user.name)
                .putString(KEY_USER_EMAIL, user.email)
                .apply()
        }
    }

    suspend fun currentToken(): String? = withContext(Dispatchers.IO) {
        encryptedPreferences().getString(KEY_TOKEN, null)
    }

    suspend fun currentAuthorizationHeader(): String? {
        return currentToken()?.let { token -> "Bearer $token" }
    }

    suspend fun currentUser(): User? = withContext(Dispatchers.IO) {
        val storage = encryptedPreferences()
        val id = storage.getString(KEY_USER_ID, null) ?: return@withContext null
        val name = storage.getString(KEY_USER_NAME, null) ?: return@withContext null
        val email = storage.getString(KEY_USER_EMAIL, null) ?: return@withContext null
        User(id = id, name = name, email = email, password = "")
    }

    suspend fun currentUserId(): String? = withContext(Dispatchers.IO) {
        encryptedPreferences().getString(KEY_USER_ID, null)
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) { encryptedPreferences().edit().clear().apply() }
    }

    private fun encryptedPreferences(): SharedPreferences {
        return preferences ?: synchronized(preferencesLock) {
            preferences ?: createEncryptedPreferences().also { preferences = it }
        }
    }

    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            applicationContext,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private companion object {
        const val FILE_NAME = "secure_session"
        const val KEY_TOKEN = "token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
    }
}

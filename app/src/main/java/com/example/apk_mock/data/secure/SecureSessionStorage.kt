package com.example.apk_mock.data.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.apk_mock.domain.repository.User

class SecureSessionStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(token: String, user: User) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .apply()
    }

    fun currentToken(): String? {
        return preferences.getString(KEY_TOKEN, null)
    }

    fun currentAuthorizationHeader(): String? {
        return currentToken()?.let { token -> "Bearer $token" }
    }

    fun currentUser(): User? {
        val id = preferences.getString(KEY_USER_ID, null) ?: return null
        val name = preferences.getString(KEY_USER_NAME, null) ?: return null
        val email = preferences.getString(KEY_USER_EMAIL, null) ?: return null
        return User(
            id = id,
            name = name,
            email = email,
            password = ""
        )
    }

    fun currentUserId(): String? {
        return preferences.getString(KEY_USER_ID, null)
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "secure_session"
        const val KEY_TOKEN = "token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
    }
}

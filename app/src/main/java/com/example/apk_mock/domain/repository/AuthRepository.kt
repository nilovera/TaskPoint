package com.example.apk_mock.domain.repository

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class ResetResult {
    object CodeSent : ResetResult()
    object CodeValid : ResetResult()
    object PasswordChanged : ResetResult()
    data class Error(val message: String) : ResetResult()
}

sealed class ProfileResult {
    data class Success(val user: User? = null) : ProfileResult()
    data class Error(val message: String) : ProfileResult()
}

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    suspend fun currentUser(): User?
    suspend fun logout()
    suspend fun sendResetCode(email: String): ResetResult
    suspend fun verifyResetCode(email: String, code: String): ResetResult
    suspend fun changePassword(email: String, newPassword: String): ResetResult
    suspend fun changeCurrentPassword(currentPassword: String, newPassword: String): ProfileResult
    suspend fun deleteCurrentUser(): ProfileResult
}

interface UserSessionProvider {
    suspend fun currentUserId(): String?
}

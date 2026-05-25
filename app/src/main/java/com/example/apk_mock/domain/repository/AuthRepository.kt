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
    fun register(name: String, email: String, password: String): AuthResult
    fun login(email: String, password: String): AuthResult
    fun currentUser(): User?
    fun logout()
    fun sendResetCode(email: String): ResetResult
    fun verifyResetCode(email: String, code: String): ResetResult
    fun changePassword(email: String, newPassword: String): ResetResult
    fun changeCurrentPassword(currentPassword: String, newPassword: String): ProfileResult
    fun deleteCurrentUser(): ProfileResult
}

interface UserSessionProvider {
    fun currentUserId(): String?
}

package com.example.apk_mock.domain

data class User(
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

// ── Contrato del repositorio ──────────────────────────────────────────────────

interface AuthRepository {
    fun register(name: String, email: String, password: String): AuthResult
    fun login(email: String, password: String): AuthResult
    fun sendResetCode(email: String): ResetResult
    fun verifyResetCode(email: String, code: String): ResetResult
    fun changePassword(email: String, newPassword: String): ResetResult
}
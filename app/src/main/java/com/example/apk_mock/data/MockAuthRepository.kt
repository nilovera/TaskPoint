package com.example.apk_mock.data

import com.example.apk_mock.domain.AuthRepository
import com.example.apk_mock.domain.AuthResult
import com.example.apk_mock.domain.ResetResult
import com.example.apk_mock.domain.User

/**
 * Implementación mockeada en memoria.
 * - Código de recuperación siempre: "123456"
 * - No hay red real, todo vive en listas mutables.
 */
class MockAuthRepository : AuthRepository {

    private val users = mutableListOf<User>()
    // email → código pendiente de verificación
    private val pendingResetEmails = mutableSetOf<String>()
    // email → código ya verificado (puede cambiar contraseña)
    private val verifiedResetEmails = mutableSetOf<String>()

    private val mockCode = "123456"

    // ── Auth ─────────────────────────────────────────────────────────────────

    override fun register(name: String, email: String, password: String): AuthResult {
        if (users.any { it.email.equals(email, ignoreCase = true) })
            return AuthResult.Error("El correo ya está registrado.")
        val user = User(name = name, email = email, password = password)
        users.add(user)
        return AuthResult.Success(user)
    }

    override fun login(email: String, password: String): AuthResult {
        val user = users.find {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }
        return if (user != null) AuthResult.Success(user)
        else AuthResult.Error("Correo o contraseña incorrectos.\nIntentá de nuevo.")
    }

    // ── Reset password (mock: código fijo "123456") ───────────────────────────

    override fun sendResetCode(email: String): ResetResult {
        if (users.none { it.email.equals(email, ignoreCase = true) })
            return ResetResult.Error("No existe una cuenta con ese correo.")
        pendingResetEmails.add(email.lowercase())
        // En una app real aquí se dispararía el envío de mail
        return ResetResult.CodeSent
    }

    override fun verifyResetCode(email: String, code: String): ResetResult {
        if (!pendingResetEmails.contains(email.lowercase()))
            return ResetResult.Error("Primero solicitá el código de recuperación.")
        return if (code == mockCode) {
            pendingResetEmails.remove(email.lowercase())
            verifiedResetEmails.add(email.lowercase())
            ResetResult.CodeValid
        } else {
            ResetResult.Error("Código incorrecto. Intentá de nuevo.")
        }
    }

    override fun changePassword(email: String, newPassword: String): ResetResult {
        if (!verifiedResetEmails.contains(email.lowercase()))
            return ResetResult.Error("Verificá el código antes de cambiar la contraseña.")
        val index = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index == -1) return ResetResult.Error("Usuario no encontrado.")
        users[index] = users[index].copy(password = newPassword)
        verifiedResetEmails.remove(email.lowercase())
        return ResetResult.PasswordChanged
    }
}
package com.example.apk_mock.data.repository

import android.content.Context
import com.example.apk_mock.data.source.JsonDataSource
import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.AuthResult
import com.example.apk_mock.domain.repository.ProfileResult
import com.example.apk_mock.domain.repository.ResetResult
import com.example.apk_mock.domain.repository.User
import com.example.apk_mock.domain.repository.UserSessionProvider
import java.util.UUID

class JsonAuthRepository(context: Context) : AuthRepository, UserSessionProvider {

    private val users = JsonDataSource(context).loadUsers().toMutableList()
    private val pendingResetEmails = mutableSetOf<String>()
    private val verifiedResetEmails = mutableSetOf<String>()
    private var loggedUser: User? = null
    private val resetCode = "123456"

    override fun register(name: String, email: String, password: String): AuthResult {
        if (users.any { it.email.equals(email, ignoreCase = true) }) {
            return AuthResult.Error("El correo ya esta registrado.")
        }

        val user = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            password = password
        )
        users.add(user)
        return AuthResult.Success(user)
    }

    override fun login(email: String, password: String): AuthResult {
        val user = users.find {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }

        return if (user != null) {
            loggedUser = user
            AuthResult.Success(user)
        } else {
            AuthResult.Error("Correo o contraseña incorrectos.\nIntenta de nuevo.")
        }
    }

    override fun currentUser(): User? = loggedUser

    override fun currentUserId(): String? = loggedUser?.id

    override fun logout() {
        loggedUser = null
    }

    override fun sendResetCode(email: String): ResetResult {
        if (users.none { it.email.equals(email, ignoreCase = true) }) {
            return ResetResult.Error("No existe una cuenta con ese correo.")
        }
        pendingResetEmails.add(email.lowercase())
        return ResetResult.CodeSent
    }

    override fun verifyResetCode(email: String, code: String): ResetResult {
        if (!pendingResetEmails.contains(email.lowercase())) {
            return ResetResult.Error("Primero solicita el codigo de recuperacion.")
        }

        return if (code == resetCode) {
            pendingResetEmails.remove(email.lowercase())
            verifiedResetEmails.add(email.lowercase())
            ResetResult.CodeValid
        } else {
            ResetResult.Error("Codigo incorrecto. Intenta de nuevo.")
        }
    }

    override fun changePassword(email: String, newPassword: String): ResetResult {
        if (!verifiedResetEmails.contains(email.lowercase())) {
            return ResetResult.Error("Verifica el codigo antes de cambiar la contraseña.")
        }

        val index = users.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index == -1) return ResetResult.Error("Usuario no encontrado.")

        val updatedUser = users[index].copy(password = newPassword)
        users[index] = updatedUser
        if (loggedUser?.id == updatedUser.id) loggedUser = updatedUser
        verifiedResetEmails.remove(email.lowercase())
        return ResetResult.PasswordChanged
    }

    override fun changeCurrentPassword(currentPassword: String, newPassword: String): ProfileResult {
        val user = loggedUser ?: return ProfileResult.Error("No hay una sesion activa.")
        if (user.password != currentPassword) {
            return ProfileResult.Error("La contraseña ingresada es incorrecta.")
        }

        val index = users.indexOfFirst { it.id == user.id }
        if (index == -1) return ProfileResult.Error("Usuario no encontrado.")

        val updatedUser = user.copy(password = newPassword)
        users[index] = updatedUser
        loggedUser = updatedUser
        return ProfileResult.Success(updatedUser)
    }

    override fun deleteCurrentUser(): ProfileResult {
        val user = loggedUser ?: return ProfileResult.Error("No hay una sesion activa.")
        users.removeAll { it.id == user.id }
        pendingResetEmails.remove(user.email.lowercase())
        verifiedResetEmails.remove(user.email.lowercase())
        loggedUser = null
        return ProfileResult.Success()
    }
}

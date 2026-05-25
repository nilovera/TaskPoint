package com.example.taskpoint.data

import android.content.Context
import com.example.taskpoint.domain.AuthRepository
import com.example.taskpoint.domain.AuthResult
import com.example.taskpoint.domain.ResetResult
import com.example.taskpoint.domain.User
import org.json.JSONArray

/**
 * Implementación mockeada con JSON.
 * - Carga usuarios iniciales desde assets/usuarios.json
 * - Los usuarios nuevos (registrados en sesión) se agregan en memoria
 * - Código de recuperación siempre: "123456"
 */
class MockAuthRepository(private val context: Context) : AuthRepository {

    // Usuarios cargados del JSON + los que se registren en sesión
    private val users = mutableListOf<User>()
    private val pendingResetEmails = mutableSetOf<String>()
    private val verifiedResetEmails = mutableSetOf<String>()
    private val mockCode = "123456"

    init {
        loadUsersFromJson()
    }

    // ── Carga desde JSON ──────────────────────────────────────────────────────

    private fun loadUsersFromJson() {
        try {
            val json = context.assets.open("usuarios.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                users.add(
                    User(
                        name     = obj.getString("name"),
                        email    = obj.getString("email"),
                        password = obj.getString("password")
                    )
                )
            }
        } catch (e: Exception) {
            // Si el JSON falla, la app arranca sin usuarios precargados
            e.printStackTrace()
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

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

    // ── Reset password ────────────────────────────────────────────────────────

    override fun sendResetCode(email: String): ResetResult {
        if (users.none { it.email.equals(email, ignoreCase = true) })
            return ResetResult.Error("No existe una cuenta con ese correo.")
        pendingResetEmails.add(email.lowercase())
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
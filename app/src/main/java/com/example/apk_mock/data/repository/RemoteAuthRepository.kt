package com.example.apk_mock.data.repository

import com.example.apk_mock.data.remote.AuthApi
import com.example.apk_mock.data.remote.dto.ChangeCurrentPasswordRequestDto
import com.example.apk_mock.data.remote.dto.ChangePasswordRequestDto
import com.example.apk_mock.data.remote.dto.LoginRequestDto
import com.example.apk_mock.data.remote.dto.RegisterRequestDto
import com.example.apk_mock.data.remote.dto.SendResetCodeRequestDto
import com.example.apk_mock.data.remote.dto.UserDto
import com.example.apk_mock.data.remote.dto.VerifyResetCodeRequestDto
import com.example.apk_mock.data.secure.SecureSessionStorage
import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.AuthResult
import com.example.apk_mock.domain.repository.ProfileResult
import com.example.apk_mock.domain.repository.ResetResult
import com.example.apk_mock.domain.repository.User
import com.example.apk_mock.domain.repository.UserSessionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.io.IOException

class RemoteAuthRepository(
    private val authApi: AuthApi,
    private val sessionStorage: SecureSessionStorage
) : AuthRepository, UserSessionProvider {

    override fun register(name: String, email: String, password: String): AuthResult {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val response = authApi.register(
                    RegisterRequestDto(
                        name = name,
                        email = email,
                        password = password
                    )
                )
                val user = response.user.toDomainUser()
                sessionStorage.saveSession(response.token, user)
                AuthResult.Success(user)
            }.getOrElse { error ->
                AuthResult.Error(error.toUserMessage("No se pudo registrar la cuenta."))
            }
        }
    }

    override fun login(email: String, password: String): AuthResult {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val response = authApi.login(LoginRequestDto(email = email, password = password))
                val user = response.user.toDomainUser()
                sessionStorage.saveSession(response.token, user)
                AuthResult.Success(user)
            }.getOrElse { error ->
                AuthResult.Error(error.toUserMessage("Correo o contraseña incorrectos."))
            }
        }
    }

    override fun currentUser(): User? {
        return sessionStorage.currentUser()
    }

    override fun currentUserId(): String? {
        return sessionStorage.currentUserId()
    }

    override fun logout() {
        sessionStorage.clear()
    }

    override fun sendResetCode(email: String): ResetResult {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                authApi.sendResetCode(SendResetCodeRequestDto(email = email))
                ResetResult.CodeSent
            }.getOrElse { error ->
                ResetResult.Error(error.toUserMessage("No se pudo enviar el codigo de recuperacion."))
            }
        }
    }

    override fun verifyResetCode(email: String, code: String): ResetResult {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                authApi.verifyResetCode(VerifyResetCodeRequestDto(email = email, code = code))
                ResetResult.CodeValid
            }.getOrElse { error ->
                ResetResult.Error(error.toUserMessage("Codigo incorrecto. Intenta de nuevo."))
            }
        }
    }

    override fun changePassword(email: String, newPassword: String): ResetResult {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                authApi.changePassword(
                    ChangePasswordRequestDto(
                        email = email,
                        newPassword = newPassword
                    )
                )
                ResetResult.PasswordChanged
            }.getOrElse { error ->
                ResetResult.Error(error.toUserMessage("No se pudo cambiar la contraseña."))
            }
        }
    }

    override fun changeCurrentPassword(currentPassword: String, newPassword: String): ProfileResult {
        val authorization = sessionStorage.currentAuthorizationHeader()
            ?: return ProfileResult.Error("No hay una sesion activa.")

        return runBlocking(Dispatchers.IO) {
            runCatching {
                authApi.changeCurrentPassword(
                    authorization = authorization,
                    body = ChangeCurrentPasswordRequestDto(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                )
                ProfileResult.Success(sessionStorage.currentUser())
            }.getOrElse { error ->
                ProfileResult.Error(error.toUserMessage("No se pudo cambiar la contraseña."))
            }
        }
    }

    override fun deleteCurrentUser(): ProfileResult {
        val authorization = sessionStorage.currentAuthorizationHeader()
            ?: return ProfileResult.Error("No hay una sesion activa.")

        return runBlocking(Dispatchers.IO) {
            runCatching {
                authApi.deleteCurrentUser(authorization)
                sessionStorage.clear()
                ProfileResult.Success()
            }.getOrElse { error ->
                ProfileResult.Error(error.toUserMessage("No se pudo eliminar la cuenta."))
            }
        }
    }

    private fun UserDto.toDomainUser(): User {
        return User(
            id = id,
            name = name,
            email = email,
            password = ""
        )
    }

    private fun Throwable.toUserMessage(defaultMessage: String): String {
        return when (this) {
            is IOException -> "No se pudo conectar con el servidor."
            is HttpException -> when (code()) {
                400 -> "Revisa los datos ingresados."
                401 -> "Credenciales incorrectas o sesion vencida."
                403 -> "No tenes permisos para realizar esta accion."
                404 -> "No se encontro el recurso solicitado."
                409 -> "Ya existe un registro con esos datos."
                in 500..599 -> "El servidor no pudo procesar la solicitud."
                else -> defaultMessage
            }
            else -> defaultMessage
        }
    }
}

package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.ProfileResult
import com.example.apk_mock.domain.repository.User

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): User? = repository.currentUser()
}

class LogoutUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.logout()
}

class ChangeCurrentPasswordUseCase(private val repository: AuthRepository) {
    operator fun invoke(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): ProfileResult {
        if (currentPassword.isBlank()) {
            return ProfileResult.Error("Ingresa tu contraseña actual.")
        }
        val currentUser = repository.currentUser()
            ?: return ProfileResult.Error("No hay una sesion activa.")
        if (currentUser.password != currentPassword) {
            return ProfileResult.Error("La contraseña ingresada es incorrecta.")
        }
        if (newPassword.length < 6) {
            return ProfileResult.Error("Tu contraseña debe tener mas de 6 caracteres.")
        }
        if (newPassword != confirmPassword) {
            return ProfileResult.Error("Las contraseñas ingresadas no coinciden entre si.")
        }
        return repository.changeCurrentPassword(currentPassword, newPassword)
    }
}

class DeleteAccountUseCase(private val repository: AuthRepository) {
    operator fun invoke(): ProfileResult = repository.deleteCurrentUser()
}

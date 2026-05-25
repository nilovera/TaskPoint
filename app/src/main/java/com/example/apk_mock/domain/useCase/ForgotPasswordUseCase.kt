package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.ResetResult

class SendResetCodeUseCase(private val repository: AuthRepository) {
    operator fun invoke(email: String): ResetResult {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return ResetResult.Error("Ingresá un correo válido.")
        return repository.sendResetCode(email.trim())
    }
}

class VerifyResetCodeUseCase(private val repository: AuthRepository) {
    operator fun invoke(email: String, code: String): ResetResult {
        if (code.length != 6) return ResetResult.Error("El código debe tener 6 dígitos.")
        return repository.verifyResetCode(email, code)
    }
}

class ChangePasswordUseCase(private val repository: AuthRepository) {
    operator fun invoke(email: String, newPassword: String, confirm: String): ResetResult {
        if (newPassword.length < 6)
            return ResetResult.Error("La contraseña debe tener al menos 6 caracteres.")
        if (newPassword != confirm)
            return ResetResult.Error("Las contraseñas no coinciden.")
        return repository.changePassword(email, newPassword)
    }
}

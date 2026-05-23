package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.AuthRepository
import com.example.apk_mock.domain.AuthResult

class RegisterUseCase(private val repository: AuthRepository) {
    operator fun invoke(name: String, email: String, password: String): AuthResult {
        if (name.isBlank()) return AuthResult.Error("Ingresá tu nombre completo.")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return AuthResult.Error("Ingresá un correo válido.")
        if (password.length < 6) return AuthResult.Error("La contraseña debe tener al menos 6 caracteres.")
        return repository.register(name.trim(), email.trim(), password)
    }
}
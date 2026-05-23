package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.AuthRepository
import com.example.apk_mock.domain.AuthResult

class LoginUseCase(private val repository: AuthRepository) {
    operator fun invoke(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank())
            return AuthResult.Error("Completá todos los campos.")
        return repository.login(email.trim(), password)
    }
}
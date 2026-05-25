package com.example.taskpoint.domain.useCase

import com.example.taskpoint.domain.AuthRepository
import com.example.taskpoint.domain.AuthResult

class LoginUseCase(private val repository: AuthRepository) {
    operator fun invoke(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank())
            return AuthResult.Error("Completá todos los campos.")
        return repository.login(email.trim(), password)
    }
}
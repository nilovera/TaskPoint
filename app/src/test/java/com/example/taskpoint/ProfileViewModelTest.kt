package com.example.taskpoint

import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.AuthResult
import com.example.apk_mock.domain.repository.ProfileResult
import com.example.apk_mock.domain.repository.ResetResult
import com.example.apk_mock.domain.repository.User
import com.example.apk_mock.domain.useCase.ChangeCurrentPasswordUseCase
import com.example.apk_mock.domain.useCase.DeleteAccountUseCase
import com.example.apk_mock.domain.useCase.GetCurrentUserUseCase
import com.example.apk_mock.domain.useCase.LogoutUseCase
import com.example.apk_mock.ui.profile.ProfileViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ProfileViewModelTest {

    @Test
    fun changingPasswordWithWrongCurrentPasswordShowsCurrentPasswordError() {
        val repository = FakeAuthRepository(
            savedUser = User(
                id = "user-1",
                name = "Nicolas Perez",
                email = "nico@ejemplo.com",
                password = "correcta123"
            )
        )
        val viewModel = ProfileViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
            changeCurrentPasswordUseCase = ChangeCurrentPasswordUseCase(repository),
            deleteAccountUseCase = DeleteAccountUseCase(repository)
        )

        viewModel.onCurrentPasswordChange("incorrecta")
        viewModel.onNewPasswordChange("nueva123")
        viewModel.onConfirmPasswordChange("nueva123")
        viewModel.onSavePasswordClick()

        val state = viewModel.uiState.value
        assertEquals("La contraseña ingresada es incorrecta.", state.currentPasswordError)
        assertFalse(state.showPasswordSavedMessage)
        assertFalse(repository.passwordWasChanged)
    }

    @Test
    fun changingPasswordWithDifferentConfirmationShowsConfirmPasswordError() {
        val repository = FakeAuthRepository(
            savedUser = User(
                id = "user-1",
                name = "Nicolas Perez",
                email = "nico@ejemplo.com",
                password = "correcta123"
            )
        )
        val viewModel = ProfileViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
            changeCurrentPasswordUseCase = ChangeCurrentPasswordUseCase(repository),
            deleteAccountUseCase = DeleteAccountUseCase(repository)
        )

        viewModel.onCurrentPasswordChange("correcta123")
        viewModel.onNewPasswordChange("nueva123")
        viewModel.onConfirmPasswordChange("distinta123")
        viewModel.onSavePasswordClick()

        val state = viewModel.uiState.value
        assertEquals("Las contraseñas ingresadas no coinciden entre si.", state.confirmPasswordError)
        assertEquals(null, state.currentPasswordError)
        assertFalse(state.showPasswordSavedMessage)
        assertFalse(repository.passwordWasChanged)
    }
}

private class FakeAuthRepository(
    private var savedUser: User?
) : AuthRepository {
    var passwordWasChanged = false
        private set

    override fun register(name: String, email: String, password: String): AuthResult =
        AuthResult.Error("No usado en este test.")

    override fun login(email: String, password: String): AuthResult =
        AuthResult.Error("No usado en este test.")

    override fun currentUser(): User? = savedUser

    override fun logout() {
        savedUser = null
    }

    override fun sendResetCode(email: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override fun verifyResetCode(email: String, code: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override fun changePassword(email: String, newPassword: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override fun changeCurrentPassword(
        currentPassword: String,
        newPassword: String
    ): ProfileResult {
        val user = savedUser ?: return ProfileResult.Error("No hay una sesion activa.")
        if (user.password != currentPassword) {
            return ProfileResult.Error("La contraseña ingresada es incorrecta.")
        }
        passwordWasChanged = true
        savedUser = user.copy(password = newPassword)
        return ProfileResult.Success(savedUser)
    }

    override fun deleteCurrentUser(): ProfileResult {
        savedUser = null
        return ProfileResult.Success()
    }
}

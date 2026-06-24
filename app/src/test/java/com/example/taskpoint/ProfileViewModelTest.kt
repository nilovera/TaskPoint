package com.example.taskpoint

import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.AuthResult
import com.example.apk_mock.domain.repository.ProfileResult
import com.example.apk_mock.domain.repository.ResetResult
import com.example.apk_mock.domain.repository.User
import com.example.apk_mock.ui.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun changingPasswordWithWrongCurrentPasswordShowsCurrentPasswordError() {
        val repository = FakeAuthRepository(
            savedUser = User(
                id = "user-1",
                name = "Nicolas Perez",
                email = "usuario@ejemplo.com",
                password = "correcta123"
            )
        )
        val viewModel = ProfileViewModel(repository)

        viewModel.onCurrentPasswordChange("incorrecta")
        viewModel.onNewPasswordChange("nueva123")
        viewModel.onConfirmPasswordChange("nueva123")
        viewModel.onSavePasswordClick()

        val state = viewModel.uiState.value
        assertEquals("La contrasena ingresada es incorrecta.", state.currentPasswordError)
        assertFalse(state.showPasswordSavedMessage)
        assertFalse(repository.passwordWasChanged)
    }

    @Test
    fun changingPasswordWithDifferentConfirmationShowsConfirmPasswordError() {
        val repository = FakeAuthRepository(
            savedUser = User(
                id = "user-1",
                name = "Nicolas Perez",
                email = "usuario@ejemplo.com",
                password = "correcta123"
            )
        )
        val viewModel = ProfileViewModel(repository)

        viewModel.onCurrentPasswordChange("correcta123")
        viewModel.onNewPasswordChange("nueva123")
        viewModel.onConfirmPasswordChange("distinta123")
        viewModel.onSavePasswordClick()

        val state = viewModel.uiState.value
        assertEquals("Las contrasenas ingresadas no coinciden entre si.", state.confirmPasswordError)
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

    override suspend fun register(name: String, email: String, password: String): AuthResult =
        AuthResult.Error("No usado en este test.")

    override suspend fun login(email: String, password: String): AuthResult =
        AuthResult.Error("No usado en este test.")

    override suspend fun currentUser(): User? = savedUser

    override suspend fun logout() {
        savedUser = null
    }

    override suspend fun sendResetCode(email: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override suspend fun verifyResetCode(email: String, code: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override suspend fun changePassword(email: String, newPassword: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override suspend fun changeCurrentPassword(
        currentPassword: String,
        newPassword: String
    ): ProfileResult {
        val user = savedUser ?: return ProfileResult.Error("No hay una sesion activa.")
        if (user.password != currentPassword) {
            return ProfileResult.Error("La contrasena ingresada es incorrecta.")
        }
        passwordWasChanged = true
        savedUser = user.copy(password = newPassword)
        return ProfileResult.Success(savedUser)
    }

    override suspend fun deleteCurrentUser(): ProfileResult {
        savedUser = null
        return ProfileResult.Success()
    }
}

package com.example.taskpoint

import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.AuthResult
import com.example.apk_mock.domain.repository.ProfileResult
import com.example.apk_mock.domain.repository.ResetResult
import com.example.apk_mock.domain.repository.User
import com.example.apk_mock.ui.register.RegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registerWithEmptyNameMarksNameFirstAndDoesNotCallRepository() {
        val repository = FakeRegisterAuthRepository()
        val viewModel = RegisterViewModel(repository)

        viewModel.onRegisterClick()

        assertTrue(viewModel.uiState.value.nameError?.contains("nombre", ignoreCase = true) == true)
        assertFalse(repository.registerWasCalled)
    }

    @Test
    fun registerWithEmptyEmailMarksEmailAfterNameIsComplete() {
        val repository = FakeRegisterAuthRepository()
        val viewModel = RegisterViewModel(repository)

        viewModel.onNameChange("Nicolas Perez")
        viewModel.onRegisterClick()

        assertTrue(viewModel.uiState.value.emailError?.contains("correo", ignoreCase = true) == true)
        assertFalse(repository.registerWasCalled)
    }

    @Test
    fun registerWithInvalidEmailMarksEmailError() {
        val repository = FakeRegisterAuthRepository()
        val viewModel = RegisterViewModel(repository)

        viewModel.onNameChange("Nicolas Perez")
        viewModel.onEmailChange("correo-invalido")
        viewModel.onRegisterClick()

        assertTrue(viewModel.uiState.value.emailError?.contains("correo", ignoreCase = true) == true)
        assertFalse(repository.registerWasCalled)
    }

    @Test
    fun registerWithShortPasswordMarksPasswordError() {
        val repository = FakeRegisterAuthRepository()
        val viewModel = RegisterViewModel(repository)

        viewModel.onNameChange("Nicolas Perez")
        viewModel.onEmailChange("nico@ejemplo.com")
        viewModel.onPasswordChange("123")
        viewModel.onRegisterClick()

        assertTrue(viewModel.uiState.value.passwordError?.contains("6", ignoreCase = true) == true)
        assertFalse(repository.registerWasCalled)
    }

    @Test
    fun registerWithDifferentConfirmationMarksConfirmPasswordError() {
        val repository = FakeRegisterAuthRepository()
        val viewModel = RegisterViewModel(repository)

        viewModel.onNameChange("Nicolas Perez")
        viewModel.onEmailChange("nico@ejemplo.com")
        viewModel.onPasswordChange("secreta123")
        viewModel.onConfirmPasswordChange("distinta123")
        viewModel.onRegisterClick()

        assertTrue(viewModel.uiState.value.confirmPasswordError?.contains("coinciden", ignoreCase = true) == true)
        assertFalse(repository.registerWasCalled)
    }

    @Test
    fun successfulRegisterTrimsNameAndEmailAndMarksSuccess() = runTest {
        val repository = FakeRegisterAuthRepository()
        val viewModel = RegisterViewModel(repository)

        viewModel.onNameChange("  Nicolas Perez  ")
        viewModel.onEmailChange("  nico@ejemplo.com  ")
        viewModel.onPasswordChange("secreta123")
        viewModel.onConfirmPasswordChange("secreta123")
        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals("Nicolas Perez", repository.lastName)
        assertEquals("nico@ejemplo.com", repository.lastEmail)
        assertEquals("secreta123", repository.lastPassword)
    }
}

private class FakeRegisterAuthRepository : AuthRepository {
    var registerWasCalled = false
        private set
    var lastName: String? = null
        private set
    var lastEmail: String? = null
        private set
    var lastPassword: String? = null
        private set

    override suspend fun register(name: String, email: String, password: String): AuthResult {
        registerWasCalled = true
        lastName = name
        lastEmail = email
        lastPassword = password
        return AuthResult.Success(User("user-1", name, email, password))
    }

    override suspend fun login(email: String, password: String): AuthResult =
        AuthResult.Error("No usado en este test.")

    override suspend fun currentUser(): User? = null

    override suspend fun logout() = Unit

    override suspend fun sendResetCode(email: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override suspend fun verifyResetCode(email: String, code: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override suspend fun changePassword(email: String, newPassword: String): ResetResult =
        ResetResult.Error("No usado en este test.")

    override suspend fun changeCurrentPassword(
        currentPassword: String,
        newPassword: String
    ): ProfileResult = ProfileResult.Error("No usado en este test.")

    override suspend fun deleteCurrentUser(): ProfileResult =
        ProfileResult.Error("No usado en este test.")
}

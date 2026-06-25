package com.example.taskpoint

import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.AuthResult
import com.example.apk_mock.domain.repository.ProfileResult
import com.example.apk_mock.domain.repository.ResetResult
import com.example.apk_mock.domain.repository.User
import com.example.apk_mock.ui.login.LoginViewModel
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
class LoginViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginWithMissingFieldsShowsValidationError() {
        val repository = FakeLoginAuthRepository()
        val viewModel = LoginViewModel(repository)

        viewModel.onLoginClick()

        assertEquals("Completá todos los campos.", viewModel.uiState.value.errorMessage)
        assertFalse(repository.loginWasCalled)
    }

    @Test
    fun successfulLoginTrimsEmailAndStoresUser() = runTest {
        val user = User("user-1", "Nicolas Perez", "nico@ejemplo.com", "secreta123")
        val repository = FakeLoginAuthRepository(result = AuthResult.Success(user))
        val viewModel = LoginViewModel(repository)

        viewModel.onEmailChange("  nico@ejemplo.com  ")
        viewModel.onPasswordChange("secreta123")
        viewModel.onLoginClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals(user, viewModel.uiState.value.loggedInUser)
        assertEquals("nico@ejemplo.com", repository.lastEmail)
        assertEquals("secreta123", repository.lastPassword)
    }
}

private class FakeLoginAuthRepository(
    private val result: AuthResult = AuthResult.Error("Credenciales invalidas.")
) : AuthRepository {
    var loginWasCalled = false
        private set
    var lastEmail: String? = null
        private set
    var lastPassword: String? = null
        private set

    override suspend fun register(name: String, email: String, password: String): AuthResult =
        AuthResult.Error("No usado en este test.")

    override suspend fun login(email: String, password: String): AuthResult {
        loginWasCalled = true
        lastEmail = email
        lastPassword = password
        return result
    }

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

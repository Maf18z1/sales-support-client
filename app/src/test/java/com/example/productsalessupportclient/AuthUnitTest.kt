// src/test/java/com/example/productsalessupportclient/presentation/auth/AuthViewModelTest.kt
package com.example.productsalessupportclient.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.productsalessupportclient.data.repository.AuthRepository
import com.example.productsalessupportclient.data.repository.AuthSession
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        repository = mockk()
        viewModel = AuthViewModel(repository)
    }

    @Test
    fun login_success_setsSessionAndRole() = runTest {
        val session = mockk<AuthSession>()
        every { session.profile } returns mockk {
            every { role } returns "manager"
        }

        coEvery { repository.login("manager@example.com", "manager123") } returns session

        viewModel.loginEmail = "manager@example.com"
        viewModel.loginPassword = "manager123"

        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.error)
        assertSame(session, viewModel.currentSession)
        assertEquals("manager", viewModel.loggedInRole)
    }

    @Test
    fun login_wrongPassword_setsError() = runTest {
        coEvery { repository.login("user@example.com", "wrongpass") } throws
                IllegalArgumentException("Неверный email или пароль")

        viewModel.loginEmail = "user@example.com"
        viewModel.loginPassword = "wrongpass"

        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.currentSession)
        assertNull(viewModel.loggedInRole)
        assertEquals("Неверный email или пароль", viewModel.error)
    }

    @Test
    fun login_lockedAfterFiveFailures_setsLockError() = runTest {
        var attempts = 0

        coEvery { repository.login("locked@example.com", "badpass") } answers {
            attempts++
            if (attempts <= 5) {
                throw IllegalStateException("Неверный email или пароль")
            } else {
                throw IllegalStateException("Аккаунт заблокирован после 5 неудачных попыток")
            }
        }

        viewModel.loginEmail = "locked@example.com"
        viewModel.loginPassword = "badpass"

        repeat(6) {
            viewModel.login()
            advanceUntilIdle()
        }

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.currentSession)
        assertNull(viewModel.loggedInRole)
        assertEquals("Аккаунт заблокирован после 5 неудачных попыток", viewModel.error)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {

    private val dispatcher = UnconfinedTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
package com.example.productsalessupportclient.presentation.auth

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.productsalessupportclient.data.repository.AuthRepository
import com.example.productsalessupportclient.data.repository.AuthSession
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class AuthScreensUiE2eTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setLoginContent(
        viewModel: AuthViewModel,
        onLoginSuccess: (String) -> Unit = {},
        onRegisterClick: () -> Unit = {}
    ) {
        composeRule.setContent {
            MaterialTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onRegisterClick = onRegisterClick,
                    onLoginSuccess = onLoginSuccess
                )
            }
        }
    }

    private fun setRegisterContent(
        viewModel: AuthViewModel,
        onBack: () -> Unit = {}
    ) {
        composeRule.setContent {
            MaterialTheme {
                RegisterScreen(
                    viewModel = viewModel,
                    onBack = onBack
                )
            }
        }
    }

    private fun fillTextField(index: Int, value: String) {
        val nodes = composeRule.onAllNodes(hasSetTextAction())
        nodes[index].performTextClearance()
        nodes[index].performTextInput(value)
    }

    @Test
    fun login_success_triggersCallbackWithRole() {
        val repository = mockk<AuthRepository>()
        val viewModel = AuthViewModel(repository)

        val session = mockk<AuthSession>()
        every { session.profile } returns mockk {
            every { role } returns "manager"
        }

        coEvery { repository.login("manager@example.com", "manager123") } returns session

        var receivedRole: String? = null

        setLoginContent(
            viewModel = viewModel,
            onLoginSuccess = { receivedRole = it }
        )

        fillTextField(0, "manager@example.com")
        fillTextField(1, "manager123")

        composeRule.onNodeWithText("Войти").performClick()

        composeRule.waitUntil(5_000) { receivedRole == "manager" }

        assertEquals("manager", receivedRole)
        assertEquals(session, viewModel.currentSession)
        assertNull(viewModel.loggedInRole)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.error)
    }

    @Test
    fun login_wrongPassword_showsError() {
        val repository = mockk<AuthRepository>()
        val viewModel = AuthViewModel(repository)

        coEvery {
            repository.login("user@example.com", "wrongpass")
        } throws IllegalArgumentException("Неверный email или пароль")

        setLoginContent(viewModel)

        fillTextField(0, "user@example.com")
        fillTextField(1, "wrongpass")

        composeRule.onNodeWithText("Войти").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Неверный email или пароль")
            .assertIsDisplayed()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.currentSession)
        assertNull(viewModel.loggedInRole)
        assertEquals("Неверный email или пароль", viewModel.error)
    }

    @Test
    fun login_lockedAfterFiveFailures_showsLockMessage() {
        val repository = mockk<AuthRepository>()
        val viewModel = AuthViewModel(repository)

        var attempts = 0
        coEvery { repository.login("locked@example.com", "badpass") } answers {
            attempts++
            if (attempts <= 5) {
                throw IllegalStateException("Неверный email или пароль")
            } else {
                throw IllegalStateException("Аккаунт заблокирован после 5 неудачных попыток")
            }
        }

        setLoginContent(viewModel)

        fillTextField(0, "locked@example.com")
        fillTextField(1, "badpass")

        repeat(6) {
            composeRule.onNodeWithText("Войти").performClick()
            composeRule.waitForIdle()
        }

        composeRule.onNodeWithText("Аккаунт заблокирован после 5 неудачных попыток")
            .assertIsDisplayed()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.currentSession)
        assertNull(viewModel.loggedInRole)
        assertEquals("Аккаунт заблокирован после 5 неудачных попыток", viewModel.error)
    }
}
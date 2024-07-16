package dev.meloda.fast.auth.login

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.meloda.fast.auth.login.presentation.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun singInButton_isClickable() {
        composeTestRule.setContent {
            LoginScreen()
        }

        composeTestRule.onNodeWithTag(testTag = "sing_in_fab").assertHasClickAction()
    }
}

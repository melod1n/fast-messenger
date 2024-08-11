package dev.meloda.fast.auth.login

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.meloda.fast.auth.login.presentation.LogoScreen
import org.junit.Rule
import org.junit.Test

class LogoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun goNextButton_isClickable() {
        composeTestRule.setContent {
            LogoScreen()
        }

        composeTestRule.onNodeWithTag(testTag = "go_next_fab").assertHasClickAction()
    }
}

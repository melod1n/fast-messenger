package com.meloda.app.fast.tests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.meloda.fast.auth.login.presentation.LogoScreen
import org.junit.Rule
import org.junit.Test

class LoginSignInTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signInButtonIsClickable() {
        composeTestRule.setContent {
            com.meloda.fast.auth.login.presentation.LogoScreen(onAction = {})
        }

        composeTestRule.onNodeWithTag(testTag = "Sign in button").assertHasClickAction()
    }

    @Test
    fun signInButtonTriggersSignInAction() {
        var signInClicked = false

        composeTestRule.setContent {
            com.meloda.fast.auth.login.presentation.LogoScreen(
                onAction = { action ->
                    when (action) {
                        UiAction.NextClicked -> {
                            signInClicked = true
                        }

                        else -> Unit
                    }
                }
            )
        }

        composeTestRule.onNodeWithTag("Sign in button").performClick()

        assert(signInClicked)
    }
}

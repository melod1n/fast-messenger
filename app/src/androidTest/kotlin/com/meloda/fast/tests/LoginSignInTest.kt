package com.meloda.fast.tests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.meloda.fast.modules.auth.screens.login.model.LoginScreenState
import com.meloda.fast.modules.auth.screens.login.presentation.LoginSignIn
import org.junit.Rule
import org.junit.Test

class LoginSignInTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signInButtonIsClickable() {
        composeTestRule.setContent {
            LoginSignIn(
                onSignInClick = {},
                onLoginInputChanged = {},
                onPasswordInputChanged = {},
                onPasswordVisibilityButtonClicked = {},
                screenState = LoginScreenState.EMPTY
            )
        }

        composeTestRule.onNodeWithTag(testTag = "Sign in button").assertHasClickAction()
    }

    @Test
    fun signInButtonTriggersSignInAction() {
        var signInClicked = false

        composeTestRule.setContent {
            LoginSignIn(
                onSignInClick = { signInClicked = true },
                onLoginInputChanged = {},
                onPasswordInputChanged = {},
                onPasswordVisibilityButtonClicked = {},
                screenState = LoginScreenState.EMPTY
            )
        }

        composeTestRule.onNodeWithTag("Sign in button").performClick()

        assert(signInClicked)
    }
}

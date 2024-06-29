package com.meloda.app.fast.tests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.meloda.app.fast.auth.screens.login.model.LoginScreenState
import com.meloda.app.fast.auth.screens.login.model.NavigationUiAction
import com.meloda.app.fast.auth.screens.login.presentation.LoginSignIn
import org.junit.Rule
import org.junit.Test

class LoginSignInTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signInButtonIsClickable() {
        composeTestRule.setContent {
            LoginSignIn(
                onAction = {},
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
                onAction = { action ->
                    when (action) {
                        NavigationUiAction.SignInClicked -> {
                            signInClicked = true
                        }

                        else -> Unit
                    }
                },
                screenState = LoginScreenState.EMPTY
            )
        }

        composeTestRule.onNodeWithTag("Sign in button").performClick()

        assert(signInClicked)
    }
}

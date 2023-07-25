package com.meloda.fast.screens.testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class TestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestingScreen()
        }
    }

    @Preview
    @Composable
    fun TestingScreenPreview() {
        TestingScreen()
    }

    @Composable
    fun TestingScreen() {
        Text(text = "Testing text")
    }

}

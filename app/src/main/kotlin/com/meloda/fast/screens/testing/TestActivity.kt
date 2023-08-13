package com.meloda.fast.screens.testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.meloda.fast.ext.edgeToEdge
import com.meloda.fast.ui.AppTheme

class TestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge()

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
        AppTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Button(onClick = {}) {
                        Text(text = "Button")
                    }
                }
            }
        }
    }
}

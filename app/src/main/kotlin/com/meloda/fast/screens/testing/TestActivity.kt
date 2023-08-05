package com.meloda.fast.screens.testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.edgeToEdge
import com.meloda.fast.screens.settings.SettingsFragment
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
        val useDynamicColors = AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_USE_DYNAMIC_COLORS,
            SettingsFragment.DEFAULT_VALUE_USE_DYNAMIC_COLORS
        )
        AppTheme(useDynamicColors = useDynamicColors) {
            Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column {
                        Button(onClick = {}) {
                            Text(text = "Button")
                        }
                        Text(text = "Testing text")
                    }
                }
            }
        }
    }
}

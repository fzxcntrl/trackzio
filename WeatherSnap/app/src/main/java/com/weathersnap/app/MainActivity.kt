package com.weathersnap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.weathersnap.app.navigation.WeatherSnapNavGraph
import com.weathersnap.app.ui.theme.WeatherSnapTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry-point activity for WeatherSnap.
 * Annotated with @AndroidEntryPoint so Hilt can inject dependencies
 * into this activity and its hosted composables.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherSnapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherSnapNavGraph()
                }
            }
        }
    }
}

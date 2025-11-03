package com.nhd.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nhd.news.ui.theme.TinTứcThểThaoTheme
import com.nhd.news.ui.navigation.NewsNavigation
import com.nhd.news.utils.ApiTestHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Test API connection (chỉ cho development)
        // Uncomment dòng dưới để test kết nối API
        // ApiTestHelper.testApiConnection()
        
        setContent {
            TinTứcThểThaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewsApp()
                }
            }
        }
    }
}

@Composable
fun NewsApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        NewsNavigation(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewsAppPreview() {
    TinTứcThểThaoTheme {
        NewsApp()
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.DecorRepository
import com.example.ui.DecorViewModel
import com.example.ui.Screen
import com.example.ui.screens.BookingsScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.Design3DScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB + Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DecorRepository(database.bookingDao(), database.chatMessageDao())

        // 2. ViewModel instantiation using Factory
        val factory = DecorViewModelFactory(repository)
        val viewModel: DecorViewModel by viewModels { factory }

        setContent {
            MyApplicationTheme {
                MainLayoutScaffold(viewModel = viewModel)
            }
        }
    }
}

class DecorViewModelFactory(private val repository: DecorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DecorViewModel::class.java)) {
            return DecorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class representation: ${modelClass.name}")
    }
}

@Composable
fun MainLayoutScaffold(viewModel: DecorViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab 1: Catalog
                NavigationBarItem(
                    selected = viewModel.currentScreen == Screen.Catalog,
                    onClick = { viewModel.currentScreen = Screen.Catalog },
                    icon = { Icon(Icons.Default.Storefront, "Catalog") },
                    label = { Text("Cửa Hàng", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_catalog_button"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEA580C),
                        selectedTextColor = Color(0xFFEA580C),
                        indicatorColor = Color(0xFFEA580C).copy(alpha = 0.12f)
                    )
                )

                // Tab 2: Interactive 3D Canvas
                NavigationBarItem(
                    selected = viewModel.currentScreen == Screen.Design3D,
                    onClick = { viewModel.currentScreen = Screen.Design3D },
                    icon = { Icon(Icons.Default.ViewInAr, "3D Studio") },
                    label = { Text("Không Gian 3D", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_3d_button"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEA580C),
                        selectedTextColor = Color(0xFFEA580C),
                        indicatorColor = Color(0xFFEA580C).copy(alpha = 0.12f)
                    )
                )

                // Tab 3: AI Consultancy Chat
                NavigationBarItem(
                    selected = viewModel.currentScreen == Screen.Chat,
                    onClick = { viewModel.currentScreen = Screen.Chat },
                    icon = { Icon(Icons.Default.SupportAgent, "Chat AI") },
                    label = { Text("Tư Vấn AI", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_chat_button"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEA580C),
                        selectedTextColor = Color(0xFFEA580C),
                        indicatorColor = Color(0xFFEA580C).copy(alpha = 0.12f)
                    )
                )

                // Tab 4: Reservations / History Tracker
                NavigationBarItem(
                    selected = viewModel.currentScreen == Screen.Bookings,
                    onClick = { viewModel.currentScreen = Screen.Bookings },
                    icon = { Icon(Icons.Default.EventNote, "Bookings") },
                    label = { Text("Lịch Hẹn", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_bookings_button"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEA580C),
                        selectedTextColor = Color(0xFFEA580C),
                        indicatorColor = Color(0xFFEA580C).copy(alpha = 0.12f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (viewModel.currentScreen) {
                Screen.Catalog -> CatalogScreen(viewModel)
                Screen.Design3D -> Design3DScreen(viewModel)
                Screen.Chat -> ChatScreen(viewModel)
                Screen.Bookings -> BookingsScreen(viewModel)
            }
        }
    }
}

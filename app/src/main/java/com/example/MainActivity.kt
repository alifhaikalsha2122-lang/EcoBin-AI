package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.EcoStrings
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.model.AppThemeMode
import com.example.model.Screen
import com.example.ui.EcoViewModel
import com.example.ui.components.EcoBottomNavigationBar
import com.example.ui.components.EcoTopBar
import com.example.ui.navigation.EcoNavHost
import com.example.ui.theme.EcoBinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: EcoViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            EcoBinTheme(darkTheme = isDarkTheme) {
                EcoBinApp(viewModel)
            }
        }
    }
}

@Composable
fun EcoBinApp(
    viewModel: EcoViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initDatabase(context)
    }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val language by viewModel.repository.language.collectAsState()
    val userPoints by viewModel.repository.userPoints.collectAsState()

    // Handle back button for sub-screens
    BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.LOGIN) {
        viewModel.navigateTo(Screen.HOME)
    }

    val screenTitle = when (currentScreen) {
        Screen.LOGIN -> "ECOBIN AI"
        Screen.HOME -> "ECOBIN AI"
        Screen.SCANNER -> EcoStrings.scanner(language)
        Screen.MAP -> EcoStrings.map(language)
        Screen.CHALLENGES -> EcoStrings.challenges(language)
        Screen.LEADERBOARD -> EcoStrings.leaderboard(language)
        Screen.IMPACT -> EcoStrings.impact(language)
        Screen.COMMUNITY -> EcoStrings.community(language)
        Screen.PROFILE -> EcoStrings.profile(language)
        Screen.DISPOSAL_CHAT -> EcoStrings.askAi(language)
        Screen.RECYCLING_GUIDE -> EcoStrings.guide(language)
        Screen.SCHOOL_DASHBOARD -> EcoStrings.schoolDashboard(language)
        Screen.PROOF_UPLOAD -> EcoStrings.proofUpload(language)
        Screen.PRESENTATION_DEMO -> EcoStrings.presentationDemo(language)
    }

    val showChrome = currentScreen != Screen.LOGIN

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showChrome) {
                EcoTopBar(
                    title = screenTitle,
                    language = language,
                    userPoints = userPoints,
                    showBackButton = currentScreen != Screen.HOME,
                    onBack = { viewModel.navigateTo(Screen.HOME) },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onOpenDemo = { viewModel.navigateTo(Screen.PRESENTATION_DEMO) },
                    onOpenChat = { viewModel.navigateTo(Screen.DISPOSAL_CHAT) }
                )
            }
        },
        bottomBar = {
            if (showChrome) {
                EcoBottomNavigationBar(
                    currentScreen = currentScreen,
                    language = language,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        EcoNavHost(
            currentScreen = currentScreen,
            viewModel = viewModel,
            modifier = Modifier.padding(if (showChrome) innerPadding else androidx.compose.foundation.layout.PaddingValues(0.dp))
        )
    }
}

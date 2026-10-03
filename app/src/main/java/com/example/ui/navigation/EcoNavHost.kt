package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.Screen
import com.example.ui.EcoViewModel
import com.example.ui.screens.*

/**
 * Lightweight, type-safe NavHost that uses a simple state variable (currentScreen)
 * to manage switching between Home, AI Scanner, Map, Challenges, and Profile screens
 * with animated transitions and BackHandler support, without requiring external routing libraries.
 */
@Composable
fun EcoNavHost(
    currentScreen: Screen,
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    // BackHandler pops to Home whenever the user is on any secondary screen (not Login or Home)
    BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.LOGIN) {
        viewModel.navigateTo(Screen.HOME)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith
                        fadeOut(animationSpec = tween(180))
            },
            label = "screen_transition"
        ) { targetScreen ->
            when (targetScreen) {
                Screen.LOGIN -> LoginScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.HOME -> HomeScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.SCANNER -> ScannerScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.MAP -> MapScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.CHALLENGES -> ChallengesScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.PROFILE -> ProfileScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())

                // Sub-screens seamlessly integrated into the state-based NavHost
                Screen.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.IMPACT -> ImpactScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.COMMUNITY -> CommunityScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.DISPOSAL_CHAT -> DisposalChatScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.RECYCLING_GUIDE -> RecyclingGuideScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.SCHOOL_DASHBOARD -> SchoolDashboardScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.PROOF_UPLOAD -> ProofScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                Screen.PRESENTATION_DEMO -> PresentationDemoScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

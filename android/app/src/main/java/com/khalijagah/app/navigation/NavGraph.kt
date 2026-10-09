package com.khalijagah.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.khalijagah.app.ui.screens.*
import com.khalijagah.app.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object TrainResults : Screen("train_results")
    object CrowdDetail : Screen("crowd_detail")
    object About : Screen("about")
}

@Composable
fun AppNavGraph(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                uiState = uiState,
                onSourceSelect = { viewModel.selectSource(it) },
                onDestSelect = { viewModel.selectDestination(it) },
                onSwap = { viewModel.swapStations() },
                onCheckCrowd = {
                    viewModel.searchTrains()
                    navController.navigate(Screen.TrainResults.route)
                },
                onAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.TrainResults.route) {
            TrainResultsScreen(
                sourceName = uiState.sourceStation?.shortName ?: "",
                destName = uiState.destStation?.shortName ?: "",
                trains = uiState.trains,
                isLoading = uiState.trainsLoading,
                error = uiState.trainsError,
                onTrainClick = { train ->
                    viewModel.selectTrain(train)
                    navController.navigate(Screen.CrowdDetail.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CrowdDetail.route) {
            val train = uiState.selectedTrain
            if (train != null) {
                CrowdDetailScreen(
                    train = train,
                    sourceName = uiState.sourceStation?.shortName ?: "",
                    destName = uiState.destStation?.shortName ?: "",
                    crowdData = uiState.crowdData,
                    crowdLoading = uiState.crowdLoading,
                    crowdError = uiState.crowdError,
                    analysisResult = uiState.analysisResult,
                    analysisLoading = uiState.analysisLoading,
                    analysisError = uiState.analysisError,
                    onAnalyze = { viewModel.analyzeCrowd() },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

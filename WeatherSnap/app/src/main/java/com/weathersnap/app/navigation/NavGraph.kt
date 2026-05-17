package com.weathersnap.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weathersnap.app.ui.camera.CameraScreen
import com.weathersnap.app.ui.report.CreateReportScreen
import com.weathersnap.app.ui.report.CreateReportViewModel
import com.weathersnap.app.ui.reports.SavedReportsScreen
import com.weathersnap.app.ui.reports.SavedReportsViewModel
import com.weathersnap.app.ui.weather.WeatherScreen
import com.weathersnap.app.ui.weather.WeatherViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// ── Shared transition specs ─────────────────────────────────────────

private const val NAV_ANIM_DURATION = 300

private val navEnterTransition = fadeIn(tween(NAV_ANIM_DURATION)) +
        slideInHorizontally(tween(NAV_ANIM_DURATION)) { it / 4 }

private val navExitTransition = fadeOut(tween(NAV_ANIM_DURATION)) +
        slideOutHorizontally(tween(NAV_ANIM_DURATION)) { -it / 4 }

private val navPopEnterTransition = fadeIn(tween(NAV_ANIM_DURATION)) +
        slideInHorizontally(tween(NAV_ANIM_DURATION)) { -it / 4 }

private val navPopExitTransition = fadeOut(tween(NAV_ANIM_DURATION)) +
        slideOutHorizontally(tween(NAV_ANIM_DURATION)) { it / 4 }

/**
 * Root navigation graph for WeatherSnap.
 *
 * Every destination uses shared enter/exit transitions
 * (fadeIn + slideInHorizontally / fadeOut + slideOutHorizontally, 300 ms).
 */
@Composable
fun WeatherSnapNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Weather.route,
        enterTransition = { navEnterTransition },
        exitTransition = { navExitTransition },
        popEnterTransition = { navPopEnterTransition },
        popExitTransition = { navPopExitTransition }
    ) {
        // ── Weather (Home) ──────────────────────────────────────
        composable(
            route = Screen.Weather.route,
            enterTransition = { navEnterTransition },
            exitTransition = { navExitTransition },
            popEnterTransition = { navPopEnterTransition },
            popExitTransition = { navPopExitTransition }
        ) {
            val viewModel: WeatherViewModel = hiltViewModel()
            WeatherScreen(
                viewModel = viewModel,
                onNavigateToSavedReports = {
                    navController.navigate(Screen.SavedReports.route)
                },
                onNavigateToCreateReport = { data ->
                    navController.navigate(
                        Screen.CreateReport.createRoute(
                            cityName = data.cityName,
                            country = data.country,
                            temperature = data.temperature,
                            condition = data.condition,
                            humidity = data.humidity,
                            windspeed = data.windspeed,
                            pressure = data.pressure
                        )
                    )
                }
            )
        }

        // ── Create Report (receives weather data as nav args) ──
        composable(
            route = Screen.CreateReport.route,
            enterTransition = { navEnterTransition },
            exitTransition = { navExitTransition },
            popEnterTransition = { navPopEnterTransition },
            popExitTransition = { navPopExitTransition },
            arguments = listOf(
                navArgument(Screen.ARG_CITY) { type = NavType.StringType },
                navArgument(Screen.ARG_COUNTRY) { type = NavType.StringType },
                navArgument(Screen.ARG_TEMPERATURE) { type = NavType.StringType },
                navArgument(Screen.ARG_CONDITION) { type = NavType.StringType },
                navArgument(Screen.ARG_HUMIDITY) { type = NavType.StringType },
                navArgument(Screen.ARG_WINDSPEED) { type = NavType.StringType },
                navArgument(Screen.ARG_PRESSURE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val decode = { s: String -> URLDecoder.decode(s, StandardCharsets.UTF_8.toString()) }
            val args = backStackEntry.arguments!!

            val cityName = decode(args.getString(Screen.ARG_CITY)!!)
            val country = decode(args.getString(Screen.ARG_COUNTRY)!!)
            val temperature = args.getString(Screen.ARG_TEMPERATURE)!!.toDouble()
            val condition = decode(args.getString(Screen.ARG_CONDITION)!!)
            val humidity = args.getString(Screen.ARG_HUMIDITY)!!.toDouble()
            val windspeed = args.getString(Screen.ARG_WINDSPEED)!!.toDouble()
            val pressure = args.getString(Screen.ARG_PRESSURE)!!.toDouble()

            val viewModel: CreateReportViewModel = hiltViewModel()

            // Observe camera results returned via savedStateHandle
            val savedStateHandle = backStackEntry.savedStateHandle
            LaunchedEffect(Unit) {
                savedStateHandle.getStateFlow("photoPath", "").collect { path ->
                    if (path.isNotEmpty()) {
                        val originalKb = savedStateHandle.get<Long>("originalSizeKb") ?: 0L
                        val compressedKb = savedStateHandle.get<Long>("compressedSizeKb") ?: 0L
                        viewModel.onPhotoResult(path, originalKb, compressedKb)
                        // Clear to avoid re-processing on config change
                        savedStateHandle["photoPath"] = ""
                    }
                }
            }

            CreateReportScreen(
                cityName = cityName,
                country = country,
                temperature = temperature,
                condition = condition,
                humidity = humidity,
                windspeed = windspeed,
                pressure = pressure,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onCapturePhoto = { navController.navigate(Screen.Camera.route) },
                onSaveSuccess = {
                    navController.navigate(Screen.SavedReports.route) {
                        popUpTo(Screen.Weather.route) { inclusive = false }
                    }
                }
            )
        }

        // ── Camera ──────────────────────────────────────────────
        composable(
            route = Screen.Camera.route,
            enterTransition = { navEnterTransition },
            exitTransition = { navExitTransition },
            popEnterTransition = { navPopEnterTransition },
            popExitTransition = { navPopExitTransition }
        ) {
            CameraScreen(
                onClose = {
                    navController.popBackStack()
                },
                onPhotoCaptured = { filePath, originalKb, compressedKb ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.apply {
                            set("photoPath", filePath)
                            set("originalSizeKb", originalKb)
                            set("compressedSizeKb", compressedKb)
                        }
                    navController.popBackStack()
                }
            )
        }

        // ── Saved Reports ───────────────────────────────────────
        composable(
            route = Screen.SavedReports.route,
            enterTransition = { navEnterTransition },
            exitTransition = { navExitTransition },
            popEnterTransition = { navPopEnterTransition },
            popExitTransition = { navPopExitTransition }
        ) {
            val viewModel: SavedReportsViewModel = hiltViewModel()
            SavedReportsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

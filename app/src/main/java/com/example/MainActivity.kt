package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ClockEditorScreen
import com.example.ui.screens.ClockGalleryScreen
import com.example.ui.screens.CompatibilitySettingsScreen
import com.example.ui.screens.LockPreviewScreen
import com.example.ui.screens.WallpaperBrowseScreen
import com.example.ui.theme.TecnoClockStudioTheme
import com.example.ui.viewmodel.ClockStudioViewModel

object StudioRoutes {
    const val GALLERY = "gallery"
    const val WALLPAPERS = "wallpapers"
    const val EDITOR = "editor"
    const val PREVIEW = "preview"
    const val COMPATIBILITY = "compatibility"
}

private data class NavTabItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TecnoClockStudioTheme(dynamicColor = true) {
                TecnoClockStudioApp()
            }
        }
    }
}

@Composable
fun TecnoClockStudioApp(
    viewModel: ClockStudioViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshCompatibility(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    val tabs = remember {
        listOf(
            NavTabItem(
                route = StudioRoutes.GALLERY,
                labelRes = R.string.nav_gallery,
                selectedIcon = Icons.Rounded.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard
            ),
            NavTabItem(
                route = StudioRoutes.WALLPAPERS,
                labelRes = R.string.nav_wallpapers,
                selectedIcon = Icons.Rounded.Wallpaper,
                unselectedIcon = Icons.Outlined.Wallpaper
            ),
            NavTabItem(
                route = StudioRoutes.PREVIEW,
                labelRes = R.string.nav_preview,
                selectedIcon = Icons.Rounded.PhoneAndroid,
                unselectedIcon = Icons.Outlined.PhoneAndroid
            ),
            NavTabItem(
                route = StudioRoutes.COMPATIBILITY,
                labelRes = R.string.nav_compatibility,
                selectedIcon = Icons.Rounded.Tune,
                unselectedIcon = Icons.Outlined.Tune
            )
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: StudioRoutes.GALLERY
    val showTopLevelNav = currentRoute != StudioRoutes.EDITOR

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showTopLevelNav && !useNavRail) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelNav && useNavRail) {
                    NavigationRail {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("nav_rail_${tab.route}")
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = StudioRoutes.GALLERY,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(StudioRoutes.GALLERY) {
                        ClockGalleryScreen(
                            uiState = uiState,
                            onOpenEditor = { style ->
                                viewModel.selectStyle(style, applyDefaultFont = true)
                                navController.navigate(StudioRoutes.EDITOR)
                            },
                            onGoToPreview = { navigateToTab(StudioRoutes.PREVIEW) }
                        )
                    }

                    composable(StudioRoutes.WALLPAPERS) {
                        WallpaperBrowseScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    composable(StudioRoutes.EDITOR) {
                        ClockEditorScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onGoToPreview = {
                                navController.popBackStack()
                                navigateToTab(StudioRoutes.PREVIEW)
                            }
                        )
                    }

                    composable(StudioRoutes.PREVIEW) {
                        LockPreviewScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    composable(StudioRoutes.COMPATIBILITY) {
                        CompatibilitySettingsScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

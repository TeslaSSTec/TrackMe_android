package com.sonar.trackme

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sonar.trackme.screens.AuthScreen
import com.sonar.trackme.screens.HomeScreen
import com.sonar.trackme.screens.ObserveScreen
import com.sonar.trackme.screens.RegisterScreen
import com.sonar.trackme.screens.SettingsScreen
import com.sonar.trackme.screens.TargetScreen


@Preview
@Composable
fun MainLoader(state: State) {
    val navController = rememberNavController()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val sharedPrefs = State.getSharedPrefs(context)
        state.loadToken(sharedPrefs)
        state.loadServiceState(sharedPrefs)
    }
    val route = navController.currentBackStackEntryAsState().value?.destination?.route ?: "";
    val isHideNavbar =
        route == Routes.Auth().route || route == Routes.Register().route || route == Routes.Target().route

    Scaffold(
        bottomBar = {
            if (!isHideNavbar) {
                NavBar(navController, route)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home().route,
            modifier = Modifier.padding(innerPadding),
        ) {

            composable(Routes.Home().route) {
                HomeScreen(navController = navController, state)
            }

            composable(Routes.Auth().route) {
                AuthScreen(navController = navController)
            }

            composable(Routes.Register().route) {
                RegisterScreen(navController = navController)
            }

            composable(Routes.Settings().route) {
                SettingsScreen(navController = navController, state)
            }

            composable(Routes.Observe().route) {
                ObserveScreen(navController = navController, state)
            }

            composable(
                route = Routes.Target().route,
                arguments = listOf(navArgument("targetId") { type = NavType.StringType })
            ) { backStackEntry ->
                val targetId = backStackEntry.arguments?.getString("targetId")
                val user = backStackEntry.arguments?.getString("user")
                TargetScreen(targetId = targetId ?: "", user = user ?: "")
            }
        }
    }
}

data class Route(val name: String, val route: String, val icon: ImageVector)

sealed class Routes(val route: String) {
    class Home : Routes("home")
    class Auth : Routes("auth")
    class Register : Routes("register")
    class Settings : Routes("settings")
    class Observe : Routes("observe")
    class Target : Routes("target/{targetId}/{user}")
}

@Composable
fun NavBar(navController: NavController, route: String) {

    val routes = listOf(
        Route(route = Routes.Home().route, name = "главная", icon = Icons.Default.Home),
        Route(
            route = Routes.Observe().route,
            name = "наблюдение",
            icon = Icons.Default.AccountCircle
        ),
        Route(route = Routes.Settings().route, name = "параметры", icon = Icons.Outlined.Settings)
    )

    NavigationBar(modifier = Modifier.height(50.dp)) {
        //NavigationBarItem()
        routes.forEach { topLevelRoute ->
            NavigationBarItem(
                //modifier = Modifier.height(50.dp),
                icon = {
                    Icon(
                        imageVector = topLevelRoute.icon,
                        contentDescription = topLevelRoute.name
                    )
                },
                //label = { Text(topLevelRoute.name) },
                //selected = currentDestination?.hierarchy?.any { it.hasRoute(route = topLevelRoute.route) } == true,
                selected = route == topLevelRoute.route,
                //selected = true,
                onClick = {
                    navController.navigate(topLevelRoute.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }

                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

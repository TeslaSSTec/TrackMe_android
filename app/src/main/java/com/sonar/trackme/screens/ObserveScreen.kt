package com.sonar.trackme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.sonar.trackme.RequestStatus
import com.sonar.trackme.Routes
import com.sonar.trackme.State
import com.sonar.trackme.components.Loader

@Composable
fun ObserveScreen(navController: NavController, state: State) {
    val observeState = state.observeScreenState
    if (state.api.getTargetsStatus is RequestStatus.Pending) {
        Loader(true, true)
    }
    LaunchedEffect(Unit) {
        state.api.getTargets()
    }

    Scaffold(topBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Наблюдаемые", style = MaterialTheme.typography.headlineMedium)
            Button(
                onClick = {
                    navController.navigate(Routes.Settings().route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier,
                contentPadding = PaddingValues(all = 0.dp)
            ) {
                Icon(Icons.Outlined.AddCircle, "")
            }
        }
    }) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(5.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (observeState.targets.isEmpty()) {
                Text(
                    "Вы ни за кем не наблюдаете",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            observeState.targets.map {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(it.login, style = MaterialTheme.typography.titleLarge)
                        Row()
                        {
                            Button(
                                onClick = { navController.navigate("target/${it.targetId}/${it.login}") },
                                contentPadding = PaddingValues(all = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowForward, "")
                            }
                            Spacer(
                                modifier = Modifier.width(5.dp)
                            )
                            Button(
                                onClick = {},
                                contentPadding = PaddingValues(all = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Outlined.Delete, "")
                            }

                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
        }

    }

}

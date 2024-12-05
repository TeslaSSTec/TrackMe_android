package com.sonar.trackme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonar.trackme.RequestStatus
import com.sonar.trackme.Routes
import com.sonar.trackme.State
import com.sonar.trackme.components.Header
import com.sonar.trackme.components.TrackMap
import com.sonar.trackme.startLocService

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun HomeScreen(navController: NavController, state: State) {
    val getUserStatus = state.api.getDataStatus;
    val context = LocalContext.current
    val sharedPrefs = State.getSharedPrefs(context)
    val homeState = state.homeScreenState
    LaunchedEffect(Unit) {
        state.api.getUserData()
        state.api.getRecords()
    }

    LaunchedEffect(Unit) { // Эффект запускается при первом рендере
        state.api.startPeriodicRequests()
    }

    LaunchedEffect(getUserStatus) {
        if (getUserStatus is RequestStatus.Success) {
            startLocService(context)
        }
        if (getUserStatus is RequestStatus.Error) {
            if (getUserStatus.code == 401) {
                state.api.getDataStatus = RequestStatus.Pending()
                navController.navigate(Routes.Auth().route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    //Loader(isLoading = getUserStatus is RequestStatus.Loading, true)

    Scaffold(
        topBar = {
            Header {
                if (getUserStatus is RequestStatus.Success) {
                    Card() {
                        getUserStatus.data?.let {
                            Text(
                                text = it.login,
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Card(modifier = Modifier.padding(5.dp)) {
                if (state.serviceState) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp, horizontal = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            modifier = Modifier.size(50.dp),
                            contentDescription = ""
                        )
                        Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                state.changeServiceState(
                                    sharedPrefs, false
                                )
                                startLocService(context)
                            }) { Text("Выключить слежение") }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp, horizontal = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            modifier = Modifier.size(50.dp),
                            contentDescription = ""
                        )
                        Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            onClick = {
                                state.changeServiceState(
                                    sharedPrefs, true
                                )
                                startLocService(context)
                            }) { Text("Включить слежение") }
                    }
                }
            }
        }
    ) { pad ->
        TrackMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            points = homeState.mapLabels,
            cameraPositionState = homeState.cameraPositionState
        )
    }
}
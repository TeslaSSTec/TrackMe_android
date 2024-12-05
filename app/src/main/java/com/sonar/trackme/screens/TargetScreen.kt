package com.sonar.trackme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonar.trackme.State
import com.sonar.trackme.components.TrackMap

@Composable
fun TargetScreen(targetId: String, user: String, state: State = viewModel()) {
    val targetState = state.targetScreenState
    LaunchedEffect(Unit) {
        state.api.startPeriodicTargetRequests(targetId)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Наблюдение", style = MaterialTheme.typography.headlineMedium)
                Card() {
                    Text(
                        text = user,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                }
            }
        }
    ) { pad ->
        TrackMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            points = targetState.mapLabels,
            cameraPositionState = targetState.cameraPositionState
        )

    }
}
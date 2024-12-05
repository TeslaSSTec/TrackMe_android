package com.sonar.trackme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonar.trackme.Routes
import com.sonar.trackme.State

@Composable
fun SettingsScreen(navController: NavController, state: State) {
    val settingsState = state.settingsScreenState
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        settingsState.pass = "";
        settingsState.oldPass = "";
        settingsState.token = "";
        settingsState.generatedToken = ""
    }
    Scaffold(topBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Параметры", style = MaterialTheme.typography.headlineMedium)
            Button(
                onClick = {
                    val prefs = State.getSharedPrefs(context)
                    state.logout(prefs, context)
                    navController.navigate(Routes.Auth().route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier,
                contentPadding = PaddingValues(vertical = 0.dp, horizontal = 10.dp)
            ) {
                Text("Выйти")
                Spacer(modifier = Modifier.width(5.dp))
                Icon(Icons.AutoMirrored.Outlined.ExitToApp, "")
            }
        }
    }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 5.dp)
                .padding(pad),

            ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Смена пароля", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(value = settingsState.oldPass,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Старый пароль") },
                onValueChange = { settingsState.oldPass = it.trim() })
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(value = settingsState.pass,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Новый пароль") },
                onValueChange = { settingsState.pass = it.trim() })
            Spacer(modifier = Modifier.height(5.dp))
            Button(modifier = Modifier.fillMaxWidth(), onClick = {}) { Text("Сменить пароль") }

            Spacer(modifier = Modifier.height(15.dp))
            Text("Привязка подписчиков", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = settingsState.generatedToken,
                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
                label = { Text("Токен для подписки (передайте подписчику)") },
                onValueChange = {})
            Spacer(modifier = Modifier.height(5.dp))
            Button(
                onClick = { state.api.getTokenForSub() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Получить новый") }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = settingsState.token,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Вставьте токен (для слежения)") },
                onValueChange = { settingsState.token = it.trim() })
            Spacer(modifier = Modifier.height(5.dp))
            Button(
                onClick = { state.api.subscribe(settingsState.token, context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Подписаться") }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Подписчики", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 5.dp, horizontal = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("user2", style = MaterialTheme.typography.titleLarge)
                    Row()
                    {
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


        }
    }


}
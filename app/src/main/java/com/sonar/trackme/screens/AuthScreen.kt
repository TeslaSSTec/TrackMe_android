package com.sonar.trackme.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sonar.trackme.AuthData
import com.sonar.trackme.RequestStatus
import com.sonar.trackme.Routes
import com.sonar.trackme.State
import com.sonar.trackme.components.Header

@OptIn(ExperimentalFoundationApi::class)
@Preview()
@Composable
fun AuthScreen(navController: NavController, state: State = viewModel()) {
    val context = LocalContext.current
    val sharedPrefs = State.getSharedPrefs(context)
    val authStatus = state.api.authUserStatus

    LaunchedEffect(authStatus) {
        if (authStatus is RequestStatus.Success) {
            state.updateToken(authStatus.data, sharedPrefs)
            state.api.getUserData()
            navController.navigate(Routes.Home().route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
    ) {
        Header() {
            OutlinedButton(onClick = {
                navController.navigate(Routes.Register().route)
                { popUpTo(0) { inclusive = true } }
            }) { Text("Регистрация") }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Авторизация",
                fontSize = 35.sp
            )
            if (authStatus is RequestStatus.Error) {
                Text("Ошибка: ${authStatus.message}")
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = state.authScreenState.login,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Логин") },
                onValueChange = { state.authScreenState.login = it })
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = state.authScreenState.pass,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Пароль") },
                onValueChange = { state.authScreenState.pass = it })
            Spacer(modifier = Modifier.height(15.dp))
            Button(onClick = { ->
                run {
                    state.api.authUser(
                        AuthData(
                            login = state.authScreenState.login,
                            password = state.authScreenState.pass
                        )
                    )
                }
            }, enabled = state.api.authUserStatus !is RequestStatus.Loading) {
                Text("Войти")
            }
        }
    }
}
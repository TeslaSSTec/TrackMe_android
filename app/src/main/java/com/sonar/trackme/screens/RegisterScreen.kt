package com.sonar.trackme.screens

import android.widget.Toast
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
fun RegisterScreen(navController: NavController, state: State = viewModel()) {
    val context = LocalContext.current
    val registerStatus = state.api.registerUserStatus
    val registerState = state.registerScreenState

    LaunchedEffect(registerStatus) {
        if (registerStatus is RequestStatus.Success) {
            Toast.makeText(context, "Вы успешно зарегистрировались", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.Auth().route)
            { popUpTo(0) { inclusive = true } }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
    ) {
        Header() {
            OutlinedButton(onClick = {
                navController.navigate(Routes.Auth().route)
                { popUpTo(0) { inclusive = true } }
            }) { Text("Вход") }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Регистрация",
                fontSize = 35.sp
            )
            if (registerStatus is RequestStatus.Error) {
                if (registerStatus.code == 400) {
                    Text("Логин уже занят")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = registerState.login,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Логин") },
                onValueChange = { registerState.login = it.trim() })
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = registerState.pass,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Пароль (от 8 символов)") },
                onValueChange = { registerState.pass = it.trim() })
            Spacer(modifier = Modifier.height(15.dp))
            OutlinedTextField(value = registerState.pass2,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Пароль") },
                onValueChange = { registerState.pass2 = it.trim() })
            Spacer(modifier = Modifier.height(15.dp))
            Button(onClick = { ->
                run {
                    if (registerState.pass != registerState.pass2) {
                        Toast.makeText(context, "Пароли должны совпадать", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    if (registerState.pass.length < 8 || registerState.pass.length > 30) {
                        Toast.makeText(
                            context,
                            "Длина пароля от 8 до 30 символов",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (registerState.login.isEmpty() || registerState.login.length > 30) {
                        Toast.makeText(
                            context,
                            "Длина логина от 1 до 30 символов",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    state.api.registerUser(
                        AuthData(
                            login = registerState.login,
                            password = registerState.pass
                        )
                    )
                }
            }, enabled = registerStatus !is RequestStatus.Loading) {
                Text("Зарегистрироваться")
            }
        }
    }
}
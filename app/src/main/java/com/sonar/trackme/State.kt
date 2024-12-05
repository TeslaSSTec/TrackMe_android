package com.sonar.trackme

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class State : ViewModel() {

    companion object {
        fun updateTokenInSharedPref(newToken: String, sharedPreferences: SharedPreferences) {
            sharedPreferences.edit().putString("user_token", newToken).commit()
            RetrofitInstance.rebuildAPI(newToken)
        }

        fun loadTokenFromSharedPref(sharedPreferences: SharedPreferences): String {
            return (sharedPreferences.getString("user_token", null)) ?: ""
        }

        fun loadServiceStateFromSharedPref(sharedPreferences: SharedPreferences): Boolean {
            return (sharedPreferences.getBoolean("service_state", false))
        }

        fun changeServiceStateInSharedPref(sharedPreferences: SharedPreferences, newSate: Boolean) {
            sharedPreferences.edit().putBoolean("service_state", newSate).commit()
        }

        fun getSharedPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                "my_prefs",
                Context.MODE_PRIVATE
            )
        }
    }

    private var token by mutableStateOf("")
    var serviceState by mutableStateOf(false)
    val authScreenState by mutableStateOf(AuthScreenState())
    val registerScreenState by mutableStateOf(RegisterScreenState())
    val settingsScreenState by mutableStateOf(SettingsScreenState())
    val homeScreenState by mutableStateOf(HomeScreenState())
    val observeScreenState by mutableStateOf(ObserveScreenState())
    val targetScreenState by mutableStateOf(TargetScreenState())
    val api by mutableStateOf(API(this))

    fun updateToken(newToken: String, sharedPreferences: SharedPreferences) {
        token = newToken;
        updateTokenInSharedPref(newToken, sharedPreferences)
    }

    fun loadToken(sharedPreferences: SharedPreferences) {
        val loadedToken = loadTokenFromSharedPref(sharedPreferences)
        token = loadedToken
        RetrofitInstance.rebuildAPI(loadedToken)
    }

    fun loadServiceState(sharedPreferences: SharedPreferences) {
        val loaded = loadServiceStateFromSharedPref(sharedPreferences);
        serviceState = loaded
    }

    fun changeServiceState(sharedPreferences: SharedPreferences, newState: Boolean) {
        serviceState = newState
        changeServiceStateInSharedPref(sharedPreferences, newState)
    }

    fun logout(sharedPreferences: SharedPreferences, context: Context) {
        changeServiceState(sharedPreferences, false)
        updateToken("none", sharedPreferences)
        startLocService(context)
    }

}

class AuthScreenState() {
    var login by mutableStateOf("")
    var pass by mutableStateOf("")
}

class RegisterScreenState() {
    var login by mutableStateOf("")
    var pass by mutableStateOf("")
    var pass2 by mutableStateOf("")
}

class SettingsScreenState() {
    var oldPass by mutableStateOf("")
    var pass by mutableStateOf("")
    var token by mutableStateOf("")
    var generatedToken by mutableStateOf("")
}

class HomeScreenState() {
    var mapLabels by mutableStateOf(listOf<Pair<LatLng, String>>())
    var loaded by mutableStateOf(false)
    val cameraPositionState =
        CameraPositionState(
            position = CameraPosition.fromLatLngZoom(
                LatLng(
                    55.7558,
                    37.6173
                ), 15f
            )
        )


    fun loadMapModels(list: List<RecordDto>) {
        mapLabels =
            list.map { x -> Pair<LatLng, String>(LatLng(x.lat, x.lon), x.createdAt.toString()) }
    }
}

class TargetScreenState() {
    var mapLabels by mutableStateOf(listOf<Pair<LatLng, String>>())
    var loaded by mutableStateOf(false)
    val cameraPositionState =
        CameraPositionState(
            position = CameraPosition.fromLatLngZoom(
                LatLng(
                    55.7558,
                    37.6173
                ), 15f
            )
        )


    fun loadMapModels(list: List<RecordDto>) {
        mapLabels =
            list.map { x -> Pair<LatLng, String>(LatLng(x.lat, x.lon), x.createdAt.toString()) }
    }
}

class ObserveScreenState() {
    var targets by mutableStateOf(listOf<TargetDto>())
}

class API(parentState: State) : ViewModel() {
    var getDataStatus by mutableStateOf<RequestStatus<User?>>(RequestStatus.Pending())
    private val state = parentState;

    fun getUserData() {
        getDataStatus = RequestStatus.Loading()
        viewModelScope.launch {
            val resp = RetrofitInstance.api.getUser().awaitResponse()
            getDataStatus = if (resp.isSuccessful) {
                RequestStatus.Success(resp.body())
            } else {
                RequestStatus.Error(resp.code(), resp.message())
            }
        }
    }

    var authUserStatus by mutableStateOf<RequestStatus<String>>(RequestStatus.Pending())

    fun authUser(data: AuthData) {
        authUserStatus = RequestStatus.Loading()
        state.homeScreenState.loaded = false;
        viewModelScope.launch {
            val resp = RetrofitInstance.api.authUser(data).awaitResponse()
            if (resp.isSuccessful) {
                authUserStatus = RequestStatus.Success(resp.headers().get("X-Auth-Token") ?: "")
            } else {
                authUserStatus = RequestStatus.Error(resp.code(), resp.message())
            }
        }
    }

    var registerUserStatus by mutableStateOf<RequestStatus<Unit>>(RequestStatus.Pending())

    fun registerUser(data: AuthData) {
        registerUserStatus = RequestStatus.Loading()
        viewModelScope.launch {
            val resp = RetrofitInstance.api.registerUser(data).awaitResponse()
            if (resp.isSuccessful) {
                registerUserStatus = RequestStatus.Success(Unit);
            } else {
                registerUserStatus = RequestStatus.Error(resp.code(), resp.message())
            }
        }
    }

    var getRecordsStatus by mutableStateOf<RequestStatus<List<RecordDto>>>(RequestStatus.Pending())
    fun getRecords() {
        getRecordsStatus = RequestStatus.Loading()
        viewModelScope.launch {
            val resp = RetrofitInstance.api.getRecords().awaitResponse()
            if (resp.isSuccessful) {
                getRecordsStatus = RequestStatus.Success(resp.body() ?: emptyList())
                state.homeScreenState.loadMapModels(resp.body() ?: emptyList())
            } else {
                getRecordsStatus = RequestStatus.Error(resp.code(), resp.message())
            }
        }
    }

    var getTargetsStatus by mutableStateOf<RequestStatus<List<TargetDto>>>(RequestStatus.Pending())
    fun getTargets() {
        viewModelScope.launch {
            val resp = RetrofitInstance.api.getTargets().awaitResponse()
            if (resp.isSuccessful) {
                getTargetsStatus = RequestStatus.Success(resp.body() ?: emptyList())
                state.observeScreenState.targets = resp.body() ?: emptyList();
            } else {
                getTargetsStatus = RequestStatus.Error(resp.code(), resp.message())
            }
        }
    }

    var getTokenForSubStatus by mutableStateOf<RequestStatus<String>>(RequestStatus.Pending())
    fun getTokenForSub() {
        viewModelScope.launch {
            val resp = RetrofitInstance.api.getToken().awaitResponse()
            if (resp.isSuccessful) {
                getTokenForSubStatus = RequestStatus.Success(resp.body()?.token ?: "")
                state.settingsScreenState.generatedToken = resp.body()?.token ?: "";
            } else {
                getTokenForSubStatus = RequestStatus.Error(resp.code(), resp.message())
            }
        }
    }

    var subscribeStatus by mutableStateOf<RequestStatus<Unit>>(RequestStatus.Pending())
    fun subscribe(token: String, context: Context) {
        viewModelScope.launch {
            val resp = RetrofitInstance.api.subscribe(TokenDto(token)).awaitResponse()
            if (resp.isSuccessful) {
                subscribeStatus = RequestStatus.Success(Unit)
                Toast.makeText(context, "Вы подписались", Toast.LENGTH_SHORT).show()
            } else {
                subscribeStatus = RequestStatus.Error(resp.code(), resp.message())
                Toast.makeText(context, "Неверный токен подписки", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var getTargetRecordsStatus by mutableStateOf<RequestStatus<List<RecordDto>>>(RequestStatus.Pending())
    fun getTargetRecords(user: String) {
        getTargetRecordsStatus = RequestStatus.Loading()
        viewModelScope.launch {
            val resp = RetrofitInstance.api.getTargetRecords(user).awaitResponse()
            if (resp.isSuccessful) {
                getTargetRecordsStatus = RequestStatus.Success(resp.body() ?: emptyList())
                state.targetScreenState.loadMapModels(resp.body() ?: emptyList())
            } else {
                getTargetRecordsStatus = RequestStatus.Error(resp.code(), resp.message())
            }
        }
    }

    fun startPeriodicRequests() {
        viewModelScope.launch {
            while (isActive) { // Проверяем, активен ли scope
                getRecords()
                delay(5000) // Период
            }
        }
    }

    fun startPeriodicTargetRequests(targetId: String) {
        viewModelScope.launch {
            while (isActive) { // Проверяем, активен ли scope
                getTargetRecords(targetId)
                delay(5000) // Период
            }
        }
    }

}

sealed class RequestStatus<T> {
    class Pending<T> : RequestStatus<T>()
    class Loading<T> : RequestStatus<T>()
    data class Success<T>(val data: T) : RequestStatus<T>()
    data class Error<T>(val code: Int, val message: String) : RequestStatus<T>()
}
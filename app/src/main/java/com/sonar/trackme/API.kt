package com.sonar.trackme

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException
import java.util.Date
import kotlin.reflect.KProperty

data class User(
    val id: String,
    val login: String
)

data class AuthData(
    val login: String,
    val password: String
)

data class CreateRecordDto(
    val lat: Double,
    val lon: Double
)

data class RecordDto(
    val id: String,
    val createdAt: Date,
    val userId: String,
    val lat: Double,
    val lon: Double
)

data class TargetDto(
    val targetId: String,
    val login: String
)

data class TokenDto(
    val token: String
)


interface ApiService {
    @GET("user") // Здесь будет вставлен id в URL
    fun getUser(): Call<User>

    @POST("user/auth")
    fun authUser(@Body auth: AuthData): Call<Unit>

    @POST("user")
    fun registerUser(@Body auth: AuthData): Call<Unit>

    @POST("record")
    fun createRecord(@Body recordDTO: CreateRecordDto): Call<Unit>

    @GET("record")
    fun getRecords(): Call<List<RecordDto>>

    @GET("subscriber/targets")
    fun getTargets(): Call<List<TargetDto>>

    @POST("subscriber/token")
    fun getToken(): Call<TokenDto>

    @POST("subscriber")
    fun subscribe(@Body tokenDto: TokenDto): Call<Unit>

    @GET("record/{targetId}")
    fun getTargetRecords(@Path("targetId") targetId: String): Call<List<RecordDto>>
}

//FileProcesses


class AuthInterceptor(private val tokenProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider();
        val authenticatedRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authenticatedRequest)
    }
}

class ErrorHandlingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (e: IOException) {
            return Response.Builder()
                .request(chain.request())
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(503)
                .message(e.localizedMessage?.toString() ?: "")
                .body(
                    ResponseBody.create(
                        null,
                        ""
                    )
                )  // Можно передать кастомное тело ошибки, если необходимо
                .build()
        }
    }
}

class ResettableLazy<T>(private val initializer: () -> T) {
    @Volatile
    private var _value: T? = null

    // Возвращаем значение, если оно инициализировано, или инициализируем заново
    val value: T
        get() {
            if (_value == null) {
                synchronized(this) {
                    if (_value == null) {
                        _value = initializer()
                    }
                }
            }
            return _value!!
        }

    // Метод для сброса значения
    fun reset() {
        synchronized(this) {
            _value = null
        }
    }

    // Перегрузка getValue для делегата
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    // Перегрузка setValue для делегата, чтобы поддерживать запись в var
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        synchronized(this) {
            _value = newValue
        }
    }
}

// Функция для создания экземпляра ResettableLazy
fun <T> resettableLazy(initializer: () -> T) = ResettableLazy(initializer)

object RetrofitInstance {
    //private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val BASE_URL = "http://10.0.2.2:7080/"

    // Переменная для хранения токена
    private var tokenProvider: () -> String = { "" }

    // Лаунчер для создания OkHttpClient с токеном
    private fun createClient() = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { tokenProvider() }) // Передача токена
        .addInterceptor(ErrorHandlingInterceptor())
        .build()

    // Создание Retrofit с использованием ленивого делегата с возможностью сброса
    private val retrofitInstance = resettableLazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Обращение к API с ленивым делегатом
    var api: ApiService by resettableLazy { retrofitInstance.value.create(ApiService::class.java) }

    // Метод для обновления токена и пересоздания Retrofit-инстанса
    fun rebuildAPI(newToken: String) {
        tokenProvider = { newToken }
        retrofitInstance.reset() // Сбрасываем Retrofit для повторного создания
    }
}
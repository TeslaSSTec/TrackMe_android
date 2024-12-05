package com.sonar.trackme

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse


//val poolingPeriod = 60000L //1 минута
val poolingPeriod = 10000L // 30 секунд

fun startLocService(context: Context) {
    val intent = Intent(context, LocationService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Для Android 8.0 и выше используем startForegroundService
        context.startForegroundService(intent)
    } else {
        // Для версий ниже Android 8.0 используем обычный startService
        context.startService(intent)
    }
}

fun stopLocService(context: Context) {
    val intent = Intent(context, LocationService::class.java)
    context.stopService(intent)
}


class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(Dispatchers.IO) // Определяем корутинный scope


    private val locationCallback: LocationCallback =
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    serviceScope.launch {
                        try {
                            val response = RetrofitInstance.api.createRecord(
                                CreateRecordDto(
                                    lat = location.latitude,
                                    lon = location.longitude
                                )
                            ).awaitResponse()

                            if (response.isSuccessful) {
                                //Log.v("API", "Location sent successfully: ${response.body()}")
                                Log.v("SRV", "${location.latitude}, ${location.longitude}")
                            } else {
                                Log.v(
                                    "API",
                                    "Error sending location: ${response.message()}"
                                )
                            }
                        } catch (e: Exception) {
                            Log.v("API_ERR", e.localizedMessage ?: "Error in API call")
                        }
                    }
                }
            }
        }


    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startNotification()
        startTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val state = State.loadServiceStateFromSharedPref(
            getSharedPreferences(
                "my_prefs",
                Context.MODE_PRIVATE
            )
        )
        if (!state) {
            stopSelf()
        }
        return START_STICKY
    }

    private fun startNotification() {

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            // Флаг для запуска MainActivity в новом стеке задач
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

// PendingIntent для запуска MainActivity
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }
        )

        val channelId = "location_service"
        val channelName = "Location Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Сервис отслеживания")
            .setContentText("Сервис активен и работает в фоновом режиме")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true) // Делает уведомление несмахиваемым
            .setPriority(NotificationCompat.PRIORITY_LOW) // Приоритет для постоянного уведомления
            .setContentIntent(pendingIntent)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                }
            }
            .build()

        if (ContextCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Notifications not allowed", Toast.LENGTH_SHORT).show();
            stopSelf()
            return
        }

        startForeground(1, notification)
    }

    private fun startTracking() {

        // Настройка и запуск получения местоположения
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Устанавливаем приоритет
            poolingPeriod // Интервал обновления в миллисекундах (1 минута)
        ).apply {
            setMinUpdateIntervalMillis(poolingPeriod) // Минимальный интервал обновлений
            setWaitForAccurateLocation(true)  // Ожидание более точного местоположения
            setMaxUpdateDelayMillis(2 * poolingPeriod)  // Максимальная задержка для обновлений
        }.build()

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            // Разрешения получены, можно запрашивать местоположение
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}
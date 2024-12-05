package com.sonar.trackme.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline

@Composable
fun TrackMap(
    modifier: Modifier = Modifier,
    points: List<Pair<LatLng, String>>,
    cameraPositionState: CameraPositionState
) {

    // Позиция камеры
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(LatLng(55.7558, 37.6173), 15f)
//    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        // Рисуем линию трека
        Polyline(
            points = points.map { x -> x.first },
            color = androidx.compose.ui.graphics.Color.Blue,
            width = 10f
        )

//         Добавляем подписи к точкам
        points.forEach { x ->
            Marker(
                state = MarkerState(position = x.first),
                title = x.second,
                infoWindowAnchor = androidx.compose.ui.geometry.Offset(0.5f, 0f)
            )

        }
    }

}
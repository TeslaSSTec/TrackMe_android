package com.sonar.trackme.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun Logo(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.padding(vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = "",
            modifier = Modifier.size(28.dp)
        )
        Text("TrackMe", style = MaterialTheme.typography.headlineSmall)
    }
}
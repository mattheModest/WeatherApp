package com.weatherapp.ui.hourly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HourlyDetailRow(
    val hourLabel: String,
    val verdictText: String,
    val temperatureCelsius: Double,
    val temperatureDisplay: String
)

@Composable
fun HourlyDetailRow(
    row: HourlyDetailRow,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        row.temperatureCelsius >= 25.0 -> Color(0xFFFFB300) // warm amber
        row.temperatureCelsius >= 10.0 -> Color(0xFF64B5F6) // cool blue
        else                           -> Color(0xFF1565C0) // cold deep blue
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${row.hourLabel}. ${row.verdictText}. ${row.temperatureDisplay}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored left border strip
        Box(
            modifier = Modifier
                .width(3.dp)
                .heightIn(min = 56.dp)
                .fillMaxHeight()
                .background(borderColor)
        )

        // Hour label
        Text(
            text = row.hourLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(44.dp)
                .padding(start = 10.dp)
        )

        // Verdict text — fills middle
        Text(
            text = row.verdictText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        // Temperature — bold, stands out
        Text(
            text = row.temperatureDisplay,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

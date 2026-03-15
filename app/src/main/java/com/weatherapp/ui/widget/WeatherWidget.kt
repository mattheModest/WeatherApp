package com.weatherapp.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.weatherapp.data.datastore.weatherDataStore
import com.weatherapp.model.WidgetDisplayState
import timber.log.Timber

class WeatherWidget : GlanceAppWidget() {

    // SizeMode.Exact reports the actual placed size so LocalSize.current is reliable.
    // Default SizeMode.Single can report wrong/rounded dimensions, causing the
    // minimal-layout threshold to fire even for full-size widgets.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rawState = context.applicationContext.weatherDataStore.readWidgetDisplayState()
        val state = if (rawState.verdict.isEmpty()) WidgetDisplayState.EMPTY else rawState
        Timber.d("WeatherWidget: providing glance, isStale=${state.isStale}, allClear=${state.isAllClear}")
        provideContent {
            WeatherWidgetContent(state)
        }
    }

    companion object {
        suspend fun update(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(WeatherWidget::class.java)
            glanceIds.forEach { glanceId ->
                WeatherWidget().update(context, glanceId)
            }
            Timber.d("WeatherWidget.update: refreshed ${glanceIds.size} widget(s)")
        }
    }
}

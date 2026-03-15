package com.weatherapp.ui.widget

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.weatherapp.data.datastore.weatherDataStore
import com.weatherapp.model.WidgetDisplayState
import timber.log.Timber

class WeatherWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = context.applicationContext.weatherDataStore

        provideContent {
            // Observe DataStore reactively. Every write (e.g. from rotateWidgetCopy)
            // triggers a collect emission → state updates → composition re-runs →
            // Glance pushes new RemoteViews to the launcher. This is the correct
            // Glance pattern: state flows in, not read once before provideContent.
            var state by remember { mutableStateOf(WidgetDisplayState.EMPTY) }

            LaunchedEffect(Unit) {
                store.data.collect { prefs ->
                    val built = buildWidgetDisplayState(prefs)
                    state = if (built.verdict.isEmpty()) WidgetDisplayState.EMPTY else built
                    Timber.d("WeatherWidget: recomposing, verdict='${state.verdict}'")
                }
            }

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
            Timber.d("WeatherWidget.update: triggered ${glanceIds.size} widget session(s)")
        }
    }
}

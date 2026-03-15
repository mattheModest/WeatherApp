package com.weatherapp.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val KEY_WIDGET_VERDICT = stringPreferencesKey("widget_verdict")
    val KEY_BRING_LIST = stringPreferencesKey("bring_list")
    val KEY_BEST_WINDOW = stringPreferencesKey("best_window")
    val KEY_ALL_CLEAR = booleanPreferencesKey("all_clear")
    val KEY_MOOD_LINE = stringPreferencesKey("mood_line")
    val KEY_LAST_UPDATE_EPOCH = longPreferencesKey("last_update_epoch")
    val KEY_STALENESS_FLAG = booleanPreferencesKey("staleness_flag")
    val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
    val KEY_LAST_BILLING_CHECK = longPreferencesKey("last_billing_check")
    val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
    val KEY_MANUAL_LOCATION = stringPreferencesKey("manual_location")
    val KEY_SHOULD_REQUEST_NOTIFICATIONS = booleanPreferencesKey("should_request_notifications")
    val KEY_NOTIFICATIONS_PERMISSION_REQUESTED = booleanPreferencesKey("notifications_permission_requested")
    val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val KEY_CURRENT_TEMP_C = floatPreferencesKey("current_temp_c")
    val KEY_VERDICT_CANDIDATES = stringPreferencesKey("verdict_candidates")
    val KEY_MOOD_CANDIDATES = stringPreferencesKey("mood_candidates")
    val KEY_WIDGET_SELECTED_VERDICT = stringPreferencesKey("widget_selected_verdict")
    val KEY_WIDGET_SELECTED_MOOD = stringPreferencesKey("widget_selected_mood")
    val KEY_PERSONALITY_CORE = stringPreferencesKey("personality_core")
    val KEY_VERDICT_CANDIDATES_KELVIN = stringPreferencesKey("verdict_candidates_kelvin")
    val KEY_MOOD_CANDIDATES_KELVIN = stringPreferencesKey("mood_candidates_kelvin")
    val KEY_VERDICT_CANDIDATES_GRAVES = stringPreferencesKey("verdict_candidates_graves")
    val KEY_MOOD_CANDIDATES_GRAVES = stringPreferencesKey("mood_candidates_graves")
}

package com.matchplan.coach.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.matchplan.coach.data.model.AppData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "matchplan_coach_store")

/**
 * The single source of truth for all local app data.
 *
 * Everything is stored as ONE JSON string ([AppData]) inside DataStore
 * Preferences. Decoding is fully defensive: empty storage, missing fields and
 * corrupted JSON all fall back to a valid default [AppData] so the app can
 * never crash on load. All models have default values, and we use
 * ignoreUnknownKeys / coerceInputValues so schema changes are non-fatal.
 */
class AppDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
    }

    private val dataKey = stringPreferencesKey("app_data_json")

    val appDataFlow: Flow<AppData> = context.dataStore.data
        .catch { emit(emptyPreferences()) } // never propagate IO errors to UI
        .map { prefs -> decode(prefs[dataKey]) }

    private fun emptyPreferences(): Preferences =
        androidx.datastore.preferences.core.emptyPreferences()

    private fun decode(raw: String?): AppData {
        if (raw.isNullOrBlank()) return AppData()
        return try {
            json.decodeFromString(AppData.serializer(), raw)
        } catch (e: Exception) {
            // Corrupted / incompatible JSON -> safe default. No crash.
            AppData()
        }
    }

    /** Atomically read-modify-write the full app data blob. */
    suspend fun update(transform: (AppData) -> AppData) {
        context.dataStore.edit { prefs ->
            val current = decode(prefs[dataKey])
            val updated = transform(current)
            prefs[dataKey] = try {
                json.encodeToString(AppData.serializer(), updated)
            } catch (e: Exception) {
                json.encodeToString(AppData.serializer(), AppData())
            }
        }
    }

    /** Replace everything with defaults (Reset all local data). */
    suspend fun clearAll() {
        context.dataStore.edit { prefs -> prefs.remove(dataKey) }
    }
}

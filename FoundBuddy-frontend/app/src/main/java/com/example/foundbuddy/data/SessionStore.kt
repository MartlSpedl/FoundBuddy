package com.example.foundbuddy.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_store")

class SessionStore(private val context: Context) {

    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_LANGUAGE = stringPreferencesKey("language")

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun loadUserId(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_USER_ID]
        }.first()
    }

    suspend fun saveLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = lang
        }
    }

    suspend fun loadLanguage(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_LANGUAGE]
        }.first()
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            // We do NOT clear the language here so it survives logout
        }
    }
}

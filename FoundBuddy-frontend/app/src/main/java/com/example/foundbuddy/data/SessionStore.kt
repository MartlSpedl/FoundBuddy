package com.example.foundbuddy.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "session_store")

class SessionStore(private val context: Context) {

    private val KEY_USER_ID = stringPreferencesKey("user_id")

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun loadUserId(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_USER_ID]
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
        }
    }
}

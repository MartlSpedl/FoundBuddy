package com.example.foundbuddy.controller

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LanguageManager {
    private val _currentLanguage = MutableStateFlow("de")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun get(ctx: String, lang: String = _currentLanguage.value): String {
        return strings[ctx]?.get(lang) ?: ctx
    }

    private val strings = mapOf(
        "profile" to mapOf("de" to "Profil", "en" to "Profile"),
        "settings" to mapOf("de" to "Einstellungen", "en" to "Settings"),
        "edit_profile" to mapOf("de" to "Profil bearbeiten", "en" to "Edit Profile"),
        "username" to mapOf("de" to "Benutzername", "en" to "Username"),
        "email" to mapOf("de" to "E-Mail-Adresse", "en" to "Email Address"),
        "bio" to mapOf("de" to "Bio", "en" to "Bio"),
        "bio_placeholder" to mapOf("de" to "Über dich...", "en" to "About you..."),
        "save" to mapOf("de" to "Speichern", "en" to "Save"),
        "cancel" to mapOf("de" to "Abbrechen", "en" to "Cancel"),
        "delete_account" to mapOf("de" to "Account löschen", "en" to "Delete Account"),
        "delete_account_confirm" to mapOf("de" to "Möchtest du deinen Account wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.", "en" to "Are you sure you want to delete your account? This action cannot be undone."),
        "delete" to mapOf("de" to "Löschen", "en" to "Delete"),
        "language" to mapOf("de" to "Sprache", "en" to "Language"),
        "dark_mode" to mapOf("de" to "Dark Mode", "en" to "Dark Mode"),
        "error" to mapOf("de" to "Fehler", "en" to "Error"),
        "ok" to mapOf("de" to "OK", "en" to "OK"),
        "logout" to mapOf("de" to "Abmelden", "en" to "Logout"),
        "share" to mapOf("de" to "Teilen", "en" to "Share"),
        "posts" to mapOf("de" to "Beiträge", "en" to "Posts"),
        "followers" to mapOf("de" to "Follower", "en" to "Followers"),
        "following" to mapOf("de" to "Gefolgt", "en" to "Following"),
        "found" to mapOf("de" to "Gefunden", "en" to "Found"),
        "lost" to mapOf("de" to "Verloren", "en" to "Lost"),
        "no_posts" to mapOf("de" to "Keine Beiträge vorhanden", "en" to "No posts yet"),
        "app_settings" to mapOf("de" to "App-Einstellungen", "en" to "App Settings"),
        "delete_all_items" to mapOf("de" to "Alle Fundsachen löschen", "en" to "Delete All Items"),
        "save_profile" to mapOf("de" to "Profil speichern", "en" to "Save Profile")
    )
}

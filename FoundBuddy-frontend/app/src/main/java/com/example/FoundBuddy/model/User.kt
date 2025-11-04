package com.example.FoundBuddy.model

import java.util.UUID

/**
 * Repräsentiert einen Benutzer in der App.
 *
 * - id: eindeutige Kennung (UUID)
 * - userName: öffentlicher Benutzername
 * - fullName: vollständiger Name
 * - bio: kurze Beschreibung
 * - profileImageUri: Pfad zum Profilbild (optional)
 */
data class User(
    val id: String = UUID.randomUUID().toString(),
    var userName: String = "",
    var fullName: String = "",
    var bio: String = "",
    var profileImageUri: String = ""
)

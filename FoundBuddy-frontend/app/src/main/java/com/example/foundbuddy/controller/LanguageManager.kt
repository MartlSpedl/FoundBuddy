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

    fun tr(key: String, lang: String = _currentLanguage.value): String {
        return strings[key]?.get(lang) ?: key
    }

    private val strings = mapOf(
        // Profile & Settings
        "profile" to mapOf("de" to "Profil", "en" to "Profile"),
        "settings" to mapOf("de" to "Einstellungen", "en" to "Settings"),
        "edit_profile" to mapOf("de" to "Profil bearbeiten", "en" to "Edit Profile"),
        "username" to mapOf("de" to "Benutzername", "en" to "Username"),
        "email" to mapOf("de" to "E-Mail-Adresse", "en" to "Email Address"),
        "bio" to mapOf("de" to "Bio", "en" to "Bio"),
        "bio_placeholder" to mapOf("de" to "Über dich...", "en" to "About you..."),
        "bio_default" to mapOf("de" to "Lost & Found Buddy", "en" to "Lost & Found Buddy"),
        "bio_tagline" to mapOf("de" to "Hilf anderen, ihre Schätze wiederzufinden.", "en" to "Help others find their treasures."),
        "save" to mapOf("de" to "Speichern", "en" to "Save"),
        "cancel" to mapOf("de" to "Abbrechen", "en" to "Cancel"),
        "delete_account" to mapOf("de" to "Account löschen", "en" to "Delete Account"),
        "delete_account_confirm" to mapOf("de" to "Möchtest du deinen Account wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.", "en" to "Are you sure you want to delete your account? This action cannot be undone."),
        "delete" to mapOf("de" to "Löschen", "en" to "Delete"),
        "language" to mapOf("de" to "Sprache", "en" to "Language"),
        "dark_mode" to mapOf("de" to "Dark Mode", "en" to "Dark Mode"),
        "error" to mapOf("de" to "Fehler", "en" to "Error"),
        "error_unknown" to mapOf("de" to "Ein unbekannter Fehler ist aufgetreten", "en" to "An unknown error occurred"),
        "ok" to mapOf("de" to "OK", "en" to "OK"),
        "logout" to mapOf("de" to "Abmelden", "en" to "Logout"),
        "share" to mapOf("de" to "Teilen", "en" to "Share"),
        "posts" to mapOf("de" to "Beiträge", "en" to "Posts"),
        "found" to mapOf("de" to "Gefunden", "en" to "Found"),
        "lost" to mapOf("de" to "Verloren", "en" to "Lost"),
        "no_posts" to mapOf("de" to "Keine Beiträge vorhanden", "en" to "No posts yet"),
        "app_settings" to mapOf("de" to "App-Einstellungen", "en" to "App Settings"),
        "delete_all_items" to mapOf("de" to "Alle Fundsachen löschen", "en" to "Delete All Items"),
        "save_profile" to mapOf("de" to "Profil speichern", "en" to "Save Profile"),
        "guest" to mapOf("de" to "Gast", "en" to "Guest"),
        "not_logged_in" to mapOf("de" to "nicht angemeldet", "en" to "not logged in"),
        "settings_icon" to mapOf("de" to "Einstellungen", "en" to "Settings"),
        "default_profile_image" to mapOf("de" to "Standardbild", "en" to "Default image"),
        "profile_image" to mapOf("de" to "Profilbild", "en" to "Profile image"),
        "select_profile_image" to mapOf("de" to "Profilbild auswählen", "en" to "Select profile image"),
        "edit" to mapOf("de" to "Bearbeiten", "en" to "Edit"),
        "app_management" to mapOf("de" to "App-Verwaltung", "en" to "App Management"),
        "about" to mapOf("de" to "Über", "en" to "About"),
        "copyright" to mapOf("de" to "Copyright & Impressum", "en" to "Copyright & Imprint"),
        "dsgvo" to mapOf("de" to "Datenschutzerklärung", "en" to "Privacy Policy"),
        "version" to mapOf("de" to "Version", "en" to "Version"),
        "appearance" to mapOf("de" to "Erscheinungsbild", "en" to "Appearance"),
        "account_settings" to mapOf("de" to "Konto-Einstellungen", "en" to "Account Settings"),
        "legal" to mapOf("de" to "Rechtliches", "en" to "Legal"),
        "close" to mapOf("de" to "Schließen", "en" to "Close"),
        "privacy_policy_title" to mapOf("de" to "Datenschutzerklärung", "en" to "Privacy Policy"),

        // Navigation
        "discover" to mapOf("de" to "Entdecken", "en" to "Discover"),
        "favorites" to mapOf("de" to "Gemerkt", "en" to "Favorites"),
        "upload" to mapOf("de" to "Posten", "en" to "Upload"),
        "messages" to mapOf("de" to "Nachrichten", "en" to "Messages"),

        // Auth Screen
        "registration_success" to mapOf("de" to "Registrierung erfolgreich!", "en" to "Registration successful!"),
        "verification_email_sent" to mapOf("de" to "Wir haben eine Bestätigungs-E-Mail an %s gesendet. Bitte klicke auf den Link in der E-Mail, um dein Konto zu aktivieren.", "en" to "We sent a verification email to %s. Please click the link to activate your account."),
        "back_to_login" to mapOf("de" to "Zur Anmeldung", "en" to "Back to login"),
        "email_not_verified" to mapOf("de" to "E-Mail nicht bestätigt", "en" to "Email not verified"),
        "email_not_verified_desc" to mapOf("de" to "Deine E-Mail-Adresse wurde noch nicht bestätigt. Bitte prüfe dein Postfach und klicke auf den Bestätigungslink.", "en" to "Your email address has not been verified. Please check your inbox and click the confirmation link."),
        "resend_email" to mapOf("de" to "E-Mail erneut senden", "en" to "Resend email"),
        "error_sending_email" to mapOf("de" to "Fehler beim Senden der E-Mail", "en" to "Error sending email"),
        "reset_password" to mapOf("de" to "Passwort zurücksetzen", "en" to "Reset password"),
        "reset_password_desc" to mapOf("de" to "Gib deine E-Mail-Adresse ein. Wir senden dir einen Reset-Link.", "en" to "Enter your email address. We'll send you a reset link."),
        "enter_email" to mapOf("de" to "E-Mail-Adresse", "en" to "Email address"),
        "please_enter_email" to mapOf("de" to "Bitte E-Mail eingeben.", "en" to "Please enter email."),
        "reset_email_sent" to mapOf("de" to "Reset-Mail wurde gesendet (falls ein Account existiert).", "en" to "Reset email sent (if an account exists)."),
        "send_failed" to mapOf("de" to "Senden fehlgeschlagen. Bitte versuche es später.", "en" to "Send failed. Please try again later."),
        "send" to mapOf("de" to "Senden", "en" to "Send"),
        "password" to mapOf("de" to "Passwort", "en" to "Password"),
        "password_hint" to mapOf("de" to "Mind. 8 Zeichen, Groß-/Kleinbuchstaben, Zahl, Sonderzeichen", "en" to "Min. 8 characters, upper/lowercase, number, special character"),
        "forgot_password" to mapOf("de" to "Passwort vergessen?", "en" to "Forgot password?"),
        "register" to mapOf("de" to "Registrieren", "en" to "Register"),
        "login" to mapOf("de" to "Anmelden", "en" to "Login"),
        "wrong_credentials" to mapOf("de" to "Falsche E-Mail oder Passwort", "en" to "Wrong email or password"),
        "have_account" to mapOf("de" to "Du hast bereits ein Konto?", "en" to "Already have an account?"),
        "no_account" to mapOf("de" to "Du hast noch kein Konto?", "en" to "Don't have an account?"),
        "hide" to mapOf("de" to "Ausblenden", "en" to "Hide"),
        "show" to mapOf("de" to "Anzeigen", "en" to "Show"),
        "or" to mapOf("de" to "ODER", "en" to "OR"),

        // Feed Screen
        "search" to mapOf("de" to "Suche", "en" to "Search"),
        "search_placeholder" to mapOf("de" to "Finde verlorene Schätze...", "en" to "Find lost treasures..."),
        "found_items_count" to mapOf("de" to "Gefundene Gegenstände (%d)", "en" to "Found Items (%d)"),
        "lost_items_count" to mapOf("de" to "Verlorene Gegenstände (%d)", "en" to "Lost Items (%d)"),
        "other_items_count" to mapOf("de" to "Andere (%d)", "en" to "Other (%d)"),
        "no_results" to mapOf("de" to "Keine Ergebnisse", "en" to "No results"),
        "no_posts_yet" to mapOf("de" to "Noch keine Beiträge…", "en" to "No posts yet…"),
        "good_morning" to mapOf("de" to "Guten Morgen", "en" to "Good morning"),
        "good_afternoon" to mapOf("de" to "Guten Tag", "en" to "Good afternoon"),
        "good_evening" to mapOf("de" to "Guten Abend", "en" to "Good evening"),
        "hello" to mapOf("de" to "Hallo", "en" to "Hello"),
        "welcome_back" to mapOf("de" to "Willkommen zurück, %s", "en" to "Welcome back, %s"),

        // Search Screen
        "ai_image_search" to mapOf("de" to "KI-Bildersuche", "en" to "AI Image Search"),
        "search_by_content" to mapOf("de" to "Suche nach Bildinhalten (z.B. 'goldener Schlüssel')", "en" to "Search by image content (e.g. 'golden key')"),
        "search_button" to mapOf("de" to "Suchen", "en" to "Search"),
        "ai_search_running" to mapOf("de" to "KI-Suche läuft… (erster Start kann ~1–2 Min. dauern)", "en" to "AI search running… (first start may take ~1-2 min)"),
        "image_available" to mapOf("de" to "Bild verfügbar", "en" to "Image available"),

        // Detail Screen
        "back" to mapOf("de" to "Zurück", "en" to "Back"),
        "description" to mapOf("de" to "Beschreibung: %s", "en" to "Description: %s"),
        "returned" to mapOf("de" to "Zurückgegeben", "en" to "Returned"),
        "received_back" to mapOf("de" to "Zurückbekommen", "en" to "Received back"),
        "already_returned" to mapOf("de" to "Bereits zurückgegeben", "en" to "Already returned"),

        // Favorites Screen
        "your_favorites" to mapOf("de" to "Deine Favoriten", "en" to "Your Favorites"),
        "no_favorites" to mapOf("de" to "Keine Favoriten", "en" to "No Favorites"),
        "no_favorites_yet" to mapOf("de" to "Noch keine Favoriten", "en" to "No favorites yet"),
        "mark_favorites_hint" to mapOf("de" to "Markiere interessante Funde als Favorit", "en" to "Mark interesting finds as favorites"),

        // Upload Screen
        "unknown" to mapOf("de" to "Unbekannt", "en" to "Unknown"),
        "create_post" to mapOf("de" to "Beitrag erstellen", "en" to "Create Post"),
        "no_image" to mapOf("de" to "Kein Bild", "en" to "No image"),
        "upload_error" to mapOf("de" to "Fehler beim Upload", "en" to "Upload error"),
        "upload" to mapOf("de" to "Hochladen", "en" to "Upload"),
        "preview" to mapOf("de" to "Vorschau", "en" to "Preview"),
        "change" to mapOf("de" to "Ändern", "en" to "Change"),
        "what_found" to mapOf("de" to "Was hast du %s?", "en" to "What did you %s?"),
        "found_verb" to mapOf("de" to "gefunden", "en" to "find"),
        "lost_verb" to mapOf("de" to "verloren", "en" to "lose"),
        "caption_placeholder" to mapOf("de" to "Schreibe eine Bildunterschrift...", "en" to "Write a caption..."),
        "details_placeholder" to mapOf("de" to "Details wie Marke, Farbe oder Fundort...", "en" to "Details like brand, color or location..."),
        "select_image" to mapOf("de" to "Bild auswählen", "en" to "Select image"),
        "select_image_source" to mapOf("de" to "Möchtest du ein Foto machen oder ein Bild aus der Galerie auswählen?", "en" to "Do you want to take a photo or select an image from the gallery?"),
        "gallery" to mapOf("de" to "Galerie", "en" to "Gallery"),
        "camera" to mapOf("de" to "Kamera", "en" to "Camera"),
        "key" to mapOf("de" to "Schlüssel", "en" to "Key"),
        "phone" to mapOf("de" to "Handy", "en" to "Phone"),
        "wallet" to mapOf("de" to "Geldbörse", "en" to "Wallet"),
        "jacket" to mapOf("de" to "Jacke", "en" to "Jacket"),
        "headphones" to mapOf("de" to "Kopfhörer", "en" to "Headphones"),
        "student_id" to mapOf("de" to "Schülerausweis", "en" to "Student ID"),
        "other" to mapOf("de" to "Sonstiges", "en" to "Other"),

        // Chat List Screen
        "chat_requests" to mapOf("de" to "Anfragen (%d)", "en" to "Requests (%d)"),
        "no_messages" to mapOf("de" to "Noch keine Nachrichten", "en" to "No messages yet"),
        "write_to_someone" to mapOf("de" to "Schreibe jemandem über einen Beitrag!", "en" to "Write to someone about a post!"),
        "decline" to mapOf("de" to "Ablehnen", "en" to "Decline"),
        "accept" to mapOf("de" to "Annehmen", "en" to "Accept"),

        // Chat Detail Screen
        "write_message" to mapOf("de" to "Nachricht schreiben…", "en" to "Write a message..."),
        "send_message" to mapOf("de" to "Senden", "en" to "Send"),
        "referenced_post" to mapOf("de" to "Verweis auf Beitrag", "en" to "Referenced post"),

        // Item Detail Screen
        "details" to mapOf("de" to "Details", "en" to "Details"),
        "share_via" to mapOf("de" to "Teilen via", "en" to "Share via"),
        "remove_from_favorites" to mapOf("de" to "Aus Favoriten entfernen", "en" to "Remove from favorites"),
        "add_to_favorites" to mapOf("de" to "Zu Favoriten hinzufügen", "en" to "Add to favorites"),
        "add_comment" to mapOf("de" to "Kommentar hinzufügen…", "en" to "Add comment..."),
        "item_not_found" to mapOf("de" to "Item nicht gefunden", "en" to "Item not found"),
        "uploaded_by" to mapOf("de" to "Hochgeladen von %s", "en" to "Uploaded by %s"),
        "send_message_button" to mapOf("de" to "Nachricht schreiben", "en" to "Send message"),
        "status_history" to mapOf("de" to "Status-Verlauf", "en" to "Status history"),
        "current_status" to mapOf("de" to "Aktuell: %s", "en" to "Current: %s"),
        "no_status_changes" to mapOf("de" to "Noch keine Statusänderungen", "en" to "No status changes yet"),
        "comments" to mapOf("de" to "Kommentare", "en" to "Comments"),
        "change_status" to mapOf("de" to "Status ändern", "en" to "Change status"),
        "current_status_label" to mapOf("de" to "Aktueller Status: %s", "en" to "Current status: %s"),
        "no_more_status_changes" to mapOf("de" to "Keine weiteren Statusänderungen möglich.", "en" to "No more status changes possible."),
        "comment_optional" to mapOf("de" to "Kommentar (optional)", "en" to "Comment (optional)"),
        "update_status" to mapOf("de" to "Status aktualisieren", "en" to "Update status"),
        "status_change" to mapOf("de" to "%s → %s", "en" to "%s → %s"),

        // Workflow Statuses
        "gemeldet" to mapOf("de" to "Gemeldet", "en" to "Reported"),
        "in_kontakt" to mapOf("de" to "In Kontakt", "en" to "In contact"),
        "abgeschlossen" to mapOf("de" to "Abgeschlossen", "en" to "Resolved"),
        "found" to mapOf("de" to "Gefunden", "en" to "Found"),
        "lost" to mapOf("de" to "Verloren", "en" to "Lost"),
        "by_user" to mapOf("de" to "von %s", "en" to "by %s"),
        "comment_label" to mapOf("de" to "Kommentar: %s", "en" to "Comment: %s"),

        // Chat Dialog
        "message_to" to mapOf("de" to "Nachricht an %s", "en" to "Message to %s"),
        "write_message_to_contact" to mapOf("de" to "Schreibe eine Nachricht, um Kontakt aufzunehmen.", "en" to "Write a message to make contact."),
        "your_message" to mapOf("de" to "Deine Nachricht...", "en" to "Your message..."),

        // Time units
        "time_ago" to mapOf("de" to "vor %s", "en" to "%s ago"),
        "sec" to mapOf("de" to "Sek.", "en" to "sec"),
        "min" to mapOf("de" to "Min.", "en" to "min"),
        "h" to mapOf("de" to "Std.", "en" to "h"),
        "d" to mapOf("de" to "T.", "en" to "d"),
    )
}

package com.example.foundbuddy.data

import com.example.foundbuddy.model.User
import com.example.foundbuddy.model.ValidationErrorResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

/**
 * Ergebnis einer Registrierung
 */
sealed class RegistrationResult {
    data class Success(val user: User) : RegistrationResult()
    data class ValidationError(val errors: Map<String, List<String>>) : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
}

class UserRepository {
    // private val baseUrl: String = "http://10.0.2.2:8080"
    private val baseUrl: String = "https://foundbuddy-rzyh.onrender.com"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, User::class.java)
    private val listAdapter = moshi.adapter<List<User>>(listType)
    private val adapter = moshi.adapter(User::class.java)
    private val errorAdapter = moshi.adapter(ValidationErrorResponse::class.java)

    suspend fun getAll(): List<User> = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.inputStream.use {
                val json = it.bufferedReader().readText()
                listAdapter.fromJson(json) ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun create(user: User): RegistrationResult = withContext(Dispatchers.IO) {
        try {
            when (val warm = warmUpServer()) {
                is WarmUpResult.Failed -> {
                    return@withContext RegistrationResult.Error(friendlyServerStartingMessage())
                }
                WarmUpResult.Ready -> { /* weiter */ }
            }

            val url = java.net.URL("$baseUrl/api/users")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 12_000
            conn.readTimeout = 12_000

            val json = adapter.toJson(user)
            conn.outputStream.use { it.write(json.toByteArray()) }

            val responseCode = conn.responseCode

            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use {
                    val res = it.bufferedReader().readText()
                    val createdUser = adapter.fromJson(res)
                    if (createdUser != null) {
                        RegistrationResult.Success(createdUser)
                    } else {
                        RegistrationResult.Error("Fehler beim Parsen der Antwort")
                    }
                }
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                conn.errorStream.use {
                    val errorJson = it.bufferedReader().readText()
                    val errorResponse = errorAdapter.fromJson(errorJson)
                    if (errorResponse != null) {
                        RegistrationResult.ValidationError(errorResponse.errors)
                    } else {
                        RegistrationResult.Error("Validierungsfehler")
                    }
                }
            } else if (responseCode == 502 || responseCode == 503 || responseCode == 504) {
                RegistrationResult.Error(friendlyServerStartingMessage())
            } else {
                RegistrationResult.Error("Server-Fehler: $responseCode")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            RegistrationResult.Error(friendlyServerStartingMessage())
        }
    }

    suspend fun update(user: User) = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users/${user.id}")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

            val json = adapter.toJson(user)
            conn.outputStream.use { it.write(json.toByteArray()) }

            conn.inputStream.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun resendVerificationEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users/resend-verification?email=$email")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    /**
     * Startet den Passwort-Reset Flow.
     *
     * Erwarteter Backend Endpoint:
     * POST /api/users/request-password-reset?email=...
     */
    suspend fun requestPasswordReset(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            when (val warm = warmUpServer()) {
                is WarmUpResult.Failed -> return@withContext false
                WarmUpResult.Ready -> { /* weiter */ }
            }

            val encoded = java.net.URLEncoder.encode(email, "UTF-8")
            val url = java.net.URL("$baseUrl/api/users/request-password-reset?email=$encoded")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 12_000
            conn.readTimeout = 12_000

            conn.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private suspend fun warmUpServer(): WarmUpResult {
        val url = java.net.URL("$baseUrl/api/health")

        var lastCode: Int? = null
        var lastException: Exception? = null

        // 3 Versuche: 0s, 2s, 5s (Render Cold Start)
        val delays = listOf(0L, 2000L, 5000L)

        for (d in delays) {
            if (d > 0) delay(d)

            try {
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 12_000
                    readTimeout = 12_000
                }

                val code = conn.responseCode
                lastCode = code

                if (code == HttpURLConnection.HTTP_OK) {
                    return WarmUpResult.Ready
                }

                // typische Render-Codes beim Aufwachen
                if (code == 502 || code == 503 || code == 504) {
                    continue
                }

                // alles andere: nicht “Cold start”, sondern echter Fehler
                return WarmUpResult.Failed(code, null)

            } catch (e: Exception) {
                lastException = e
                // Timeouts/IO beim Starten sind normal -> nächster Versuch
                continue
            }
        }

        return WarmUpResult.Failed(lastCode, lastException)
    }

    private sealed class WarmUpResult {
        data object Ready : WarmUpResult()
        data class Failed(val code: Int?, val exception: Exception?) : WarmUpResult()
    }

    private fun friendlyServerStartingMessage(): String {
        return "Server startet gerade. Bitte warte 10–30 Sekunden und versuche es nochmal."
    }

}

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

// Result type for user operations
sealed class UserOperationResult {
    data object Success : UserOperationResult()
    data class Error(val message: String) : UserOperationResult()
}

class UserRepository {
    // private val baseUrl: String = "http://10.0.2.2:8080"
    private val baseUrl: String = "https://martlspedl-foundbuddy-backend.hf.space"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, User::class.java)
    private val listAdapter = moshi.adapter<List<User>>(listType)
    private val adapter = moshi.adapter(User::class.java)
    private val errorAdapter = moshi.adapter(ValidationErrorResponse::class.java)

    suspend fun getAll(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 90_000  // Render Cold Start kann bis zu 60s dauern
            conn.readTimeout = 90_000

            val code = conn.responseCode
            android.util.Log.d("UserRepository", "getAll() responseCode=$code")

            if (code != HttpURLConnection.HTTP_OK) {
                val body = conn.errorStream?.bufferedReader()?.readText() ?: "(kein Body)"
                android.util.Log.w("UserRepository", "getAll() Fehler $code: $body")
                return@withContext Result.failure(
                    Exception("Server antwortet mit HTTP $code. Bitte warte kurz und versuche es erneut.")
                )
            }

            conn.inputStream.use {
                val json = it.bufferedReader().readText()
                android.util.Log.d("UserRepository", "getAll() OK, ${json.length} Zeichen erhalten")
                val parsed = listAdapter.fromJson(json)
                if (parsed == null) {
                    return@withContext Result.failure(Exception("Antwort konnte nicht verarbeitet werden (JSON-Parsing fehlgeschlagen)."))
                }
                Result.success(parsed)
            }
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("UserRepository", "getAll() Timeout", e)
            Result.failure(Exception("Verbindung zum Server hat zu lange gedauert (Timeout). Bitte versuche es erneut."))
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("UserRepository", "getAll() Host nicht gefunden", e)
            Result.failure(Exception("Server nicht erreichbar. Bitte prüfe deine Internetverbindung."))
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "getAll() unbekannter Fehler", e)
            Result.failure(Exception(e.message ?: "Unbekannter Fehler beim Verbinden mit dem Server."))
        }
    }

    suspend fun create(user: User): UserOperationResult = withContext(Dispatchers.IO) {
        try {
            when (val warm = warmUpServer()) {
                is WarmUpResult.Failed -> {
                    return@withContext UserOperationResult.Error(friendlyServerStartingMessage())
                }
                WarmUpResult.Ready -> { /* weiter */ }
            }

            val url = java.net.URL("$baseUrl/api/users")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 20_000
            conn.readTimeout = 30_000  // Render braucht Zeit für Firestore + E-Mail

            val json = adapter.toJson(user)
            conn.outputStream.use { it.write(json.toByteArray()) }

            val responseCode = conn.responseCode

            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use {
                    val res = it.bufferedReader().readText()
                    val createdUser = adapter.fromJson(res)
                    if (createdUser != null) {
                        UserOperationResult.Success
                    } else {
                        UserOperationResult.Error("Fehler beim Parsen der Antwort")
                    }
                }
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                conn.errorStream.use {
                    val errorJson = it.bufferedReader().readText()
                    val errorResponse = errorAdapter.fromJson(errorJson)
                    if (errorResponse != null) {
                        UserOperationResult.Error("Validierungsfehler: ${errorResponse.errors}")
                    } else {
                        UserOperationResult.Error("Validierungsfehler")
                    }
                }
            } else if (responseCode == 502 || responseCode == 503 || responseCode == 504) {
                UserOperationResult.Error(friendlyServerStartingMessage())
            } else {
                UserOperationResult.Error("Server-Fehler: $responseCode")
            }

        } catch (e: java.net.SocketTimeoutException) {
            e.printStackTrace()
            UserOperationResult.Error(friendlyServerStartingMessage())
        } catch (e: Exception) {
            e.printStackTrace()
            UserOperationResult.Error("Registrierung fehlgeschlagen: ${e.message}")
        }
    }

    suspend fun getCurrentUser(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users/$userId")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 12_000
            conn.readTimeout = 12_000
            
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use {
                    val json = it.bufferedReader().readText()
                    adapter.fromJson(json)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun update(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users/${user.id}")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 12_000
            conn.readTimeout = 12_000

            val json = adapter.toJson(user)
            conn.outputStream.use { it.write(json.toByteArray()) }

            conn.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun resendVerificationEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users/resend-verification?email=$email")
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


    suspend fun isServerReady(): Boolean {
        return when (warmUpServer()) {
            is WarmUpResult.Ready -> true
            is WarmUpResult.Failed -> false
        }
    }

    suspend fun warmUpServer(): WarmUpResult {
        val url = java.net.URL("$baseUrl/api/health")

        var lastCode: Int? = null
        var lastException: Exception? = null

        // 6 Versuche: 0s, 2s, 5s, 10s, 20s, 30s (Render Cold Start kann bis zu 60s dauern)
        val delays = listOf(0L, 2000L, 5000L, 10000L, 20000L, 30000L)

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
                if (code == 502 || code == 503 || code == 504 || code == 500) {
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

    sealed class WarmUpResult {
        data object Ready : WarmUpResult()
        data class Failed(val code: Int?, val exception: Exception?) : WarmUpResult()
    }

    fun friendlyServerStartingMessage(): String {
        return "Server startet gerade (Render Cold Start). Dies dauert 30-60 Sekunden. Bitte versuche es in 1 Minute erneut."
    }

}

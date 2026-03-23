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
            conn.connectTimeout = 30_000  // HuggingFace Space braucht evtl. etwas beim Aufwachen
            conn.readTimeout = 30_000

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
            conn.connectTimeout = 30_000
            conn.readTimeout = 90_000  // HF kann Anfragen lange halten, wenn Space aufwacht

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
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            
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
            conn.connectTimeout = 30_000
            conn.readTimeout = 90_000

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

        // Hugging Face hält eingehende Requests oft in der Warteschlange, während der Space bootet.
        // Daher kein Loop mehr (das war Render-spezifisch), sondern einfach ein langes Timeout.
        try {
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 30_000
                readTimeout = 90_000 // Bis zu 90s warten auf HF Boot
            }

            val code = conn.responseCode

            if (code == HttpURLConnection.HTTP_OK) {
                // Prüfen ob wir wirklich das Backend erreicht haben (vermeidet HF HTML-Ladeseite)
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                if (body.contains("\"status\"") && body.contains("\"ok\"")) {
                    return WarmUpResult.Ready
                }
                // Wenn es eine 200 OK Ladeseite ist, werten wir es als Failed, da der Code nicht weiter wartet
                return WarmUpResult.Failed(code, Exception("HF Loading Page received"))
            }

            // Andere HF Codes während Boot (502, 503, 504)
            return WarmUpResult.Failed(code, null)

        } catch (e: Exception) {
            return WarmUpResult.Failed(null, e)
        }
    }

    sealed class WarmUpResult {
        data object Ready : WarmUpResult()
        data class Failed(val code: Int?, val exception: Exception?) : WarmUpResult()
    }

    fun friendlyServerStartingMessage(): String {
        return "Server Cloud Space startet gerade. Bitte versuche es in Kürze erneut."
    }

}

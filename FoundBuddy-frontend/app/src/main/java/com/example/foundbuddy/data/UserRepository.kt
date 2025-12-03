package com.example.foundbuddy.data

import com.example.foundbuddy.model.User
import com.example.foundbuddy.model.ValidationErrorResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

    private val baseUrl = "http://10.0.2.2:8080"

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
            val url = java.net.URL("$baseUrl/api/users")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

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
            } else {
                RegistrationResult.Error("Server-Fehler: $responseCode")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RegistrationResult.Error(e.message ?: "Unbekannter Fehler")
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
}

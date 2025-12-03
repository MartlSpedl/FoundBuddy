package com.example.foundbuddy.data

import com.example.foundbuddy.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {

    private val baseUrl = "http://10.0.2.2:8080"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, User::class.java)
    private val listAdapter = moshi.adapter<List<User>>(listType)
    private val adapter = moshi.adapter(User::class.java)

    suspend fun getAll(): List<User> = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users")
            val conn = url.openConnection() as java.net.HttpURLConnection
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

    suspend fun create(user: User): User? = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

            val json = adapter.toJson(user)
            conn.outputStream.use { it.write(json.toByteArray()) }

            conn.inputStream.use {
                val res = it.bufferedReader().readText()
                adapter.fromJson(res)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun update(user: User) = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/users/${user.id}")
            val conn = url.openConnection() as java.net.HttpURLConnection
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
}

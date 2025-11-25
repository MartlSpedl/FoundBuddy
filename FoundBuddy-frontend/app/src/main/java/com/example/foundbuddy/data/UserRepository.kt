package com.example.foundbuddy.data

import android.content.Context
import com.example.foundbuddy.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class UserRepository(private val context: Context) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(User::class.java)
    private val file = File(context.filesDir, "user.json")

    suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext null
        val json = file.readText()
        adapter.fromJson(json)
    }

    suspend fun saveUser(user: User) = withContext(Dispatchers.IO) {
        file.writeText(adapter.toJson(user))
    }
}

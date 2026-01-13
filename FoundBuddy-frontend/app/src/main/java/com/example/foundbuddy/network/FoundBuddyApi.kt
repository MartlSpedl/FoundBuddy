package com.example.foundbuddy.network

// Use the Retrofit Response type instead of the OkHttp response.  The OkHttp Response
// class represents a lower‑level HTTP response, whereas Retrofit wraps that into its
// own `Response<T>` type that integrates with the coroutine call adapter and
// converter factory.  Importing the wrong class causes the KAPT stubs generator to
// resolve the type to a non‑existent placeholder (error.NonExistentClass), which
// leads to compilation errors.  Therefore, we import `retrofit2.Response` here.
import retrofit2.Response

// Retrofit HTTP method annotations live in the `retrofit2.http` package.  Without
// this import, the `@GET` annotation will not be resolved and KAPT will emit
// `error.NonExistentClass` for the annotation, resulting in the error seen during
// build.  Importing this annotation fixes the issue.
import retrofit2.http.GET


interface FoundBuddyApi {

    // Wichtig: Passe diesen Endpoint an dein Backend an!
    // Ich nehme /health als einfachen Test.

    @GET("health")
    suspend fun health(): Response<String>
}
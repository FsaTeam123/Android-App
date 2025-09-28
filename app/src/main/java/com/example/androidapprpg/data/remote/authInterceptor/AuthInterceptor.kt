package com.example.androidapprpg.data.remote.authInterceptor

import com.example.androidapprpg.data.repository.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val session : SessionManager) : Interceptor {

    //lista de endpoints que não esperam Bearer Token
    private val whitelist = listOf(
        "/auth/login",
        "/auth/register"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url
        val path = url.encodedPath

        // Se a URL bate com alguma rota white-list, passa direto
        if(whitelist.any { path.contains(it) }) {
            return chain.proceed(
                original.newBuilder()
                    .header("Accept", "application/json")
                    .build()
            )
        }

        val token = session.getToken()
        val builder = original.newBuilder()
            .header("Accept", "application/json")

        if(!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())

    }
}
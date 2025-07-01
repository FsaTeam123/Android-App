package com.example.androidapprpg.data.remote.di

import com.example.androidapprpg.data.remote.services.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class) //Todas as dependências que estão nesse @Module devem viver durante o tempo de vida da aplicação.
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit() : Retrofit =
        Retrofit.Builder()
            .baseUrl("http://15.228.149.190:8085")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit) : AuthService =
        retrofit.create(AuthService::class.java)
}
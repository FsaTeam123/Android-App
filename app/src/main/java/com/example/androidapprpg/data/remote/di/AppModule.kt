package com.example.androidapprpg.data.remote.di

import com.example.androidapprpg.data.remote.services.AuthService
import com.example.androidapprpg.data.remote.services.ForgotPasswordService
import com.example.androidapprpg.data.remote.services.GameLobbyService
import com.example.androidapprpg.data.remote.services.JoinGameService
import com.example.androidapprpg.data.remote.services.MyGamesService
import com.example.androidapprpg.data.remote.services.NewGameService
import com.example.androidapprpg.webClient.services.RegisterService
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


    @Provides
    @Singleton
    fun provideCadastroService(retrofit: Retrofit) : RegisterService =
        retrofit.create(RegisterService::class.java)


    @Provides
    @Singleton
    fun provideForgotPasswordService(retrofit: Retrofit) : ForgotPasswordService =
        retrofit.create(ForgotPasswordService::class.java)

    @Provides
    @Singleton
    fun provideNewGamesService(retrofit: Retrofit) : NewGameService =
        retrofit.create(NewGameService::class.java)

    @Provides
    @Singleton
    fun provideMyGamesService(retrofit: Retrofit) : MyGamesService =
        retrofit.create(MyGamesService::class.java)

    @Provides
    @Singleton
    fun provideJoinGameService(retrofit: Retrofit) : JoinGameService =
        retrofit.create(JoinGameService::class.java)

    @Provides
    @Singleton
    fun provideGameLobbyService(retrofit: Retrofit) : GameLobbyService =
        retrofit.create(GameLobbyService::class.java)
}
package com.example.androidapprpg.data.remote.di

import com.example.androidapprpg.BuildConfig
import com.example.androidapprpg.data.remote.authInterceptor.AuthInterceptor
import com.example.androidapprpg.data.remote.services.AuthService
import com.example.androidapprpg.data.remote.services.ForgotPasswordService
import com.example.androidapprpg.data.remote.services.GameLobbyService
import com.example.androidapprpg.data.remote.services.HomeService
import com.example.androidapprpg.data.remote.services.JoinGameService
import com.example.androidapprpg.data.remote.services.MyGamesService
import com.example.androidapprpg.data.remote.services.NewGameService
import com.example.androidapprpg.data.remote.services.ProfileService
import com.example.androidapprpg.data.remote.services.spinners.CardMagiasService
import com.example.androidapprpg.data.remote.services.spinners.CardMasterService
import com.example.androidapprpg.data.remote.services.spinners.CardPlayerService
import com.example.androidapprpg.data.remote.services.spinners.CardPoderService
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.webClient.services.RegisterService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class) //Todas as dependências que estão nesse @Module devem viver durante o tempo de vida da aplicação.
object AppModule {

    // --------LOG--------------
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // --------AuthInterceptor - Bearer Token--------------
    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor =
        AuthInterceptor(sessionManager)

    // --------OKHTTP--------------
    @Provides
    @Singleton
    fun provideGameOkHttp(logging : HttpLoggingInterceptor, authInterceptor: AuthInterceptor) : OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()


    // --------RETROFIT--------------
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_GAME)
            .client(okHttpClient) //configuração do cliente do retrofit
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    // --------SERVICES INSTANCIADOS--------------

    //Login Auth
    @Provides
    @Singleton
    fun provideAuthService( retrofit: Retrofit) : AuthService =
        retrofit.create(AuthService::class.java)


    //Register Auth
    @Provides
    @Singleton
    fun provideCadastroService( retrofit: Retrofit) : RegisterService =
        retrofit.create(RegisterService::class.java)

    @Provides
    @Singleton
    fun provideNewGamesService( retrofit: Retrofit) : NewGameService =
        retrofit.create(NewGameService::class.java)

    @Provides
    @Singleton
    fun provideMyGamesService( retrofit: Retrofit) : MyGamesService =
        retrofit.create(MyGamesService::class.java)

    @Provides
    @Singleton
    fun provideJoinGameService( retrofit: Retrofit) : JoinGameService =
        retrofit.create(JoinGameService::class.java)

    @Provides
    @Singleton
    fun provideGameLobbyService( retrofit: Retrofit) : GameLobbyService =
        retrofit.create(GameLobbyService::class.java)

    @Provides
    @Singleton
    fun provideCardMasterService(retrofit: Retrofit) : CardMasterService =
        retrofit.create((CardMasterService::class.java))

    @Provides
    @Singleton
    fun provideCardPoderesService( retrofit: Retrofit) : CardPoderService =
        retrofit.create((CardPoderService::class.java))

    @Provides
    @Singleton
    fun provideCardMagiasService( retrofit: Retrofit) : CardMagiasService =
        retrofit.create(CardMagiasService::class.java)

    @Provides
    @Singleton
    fun provideCardPlayerService( retrofit: Retrofit) : CardPlayerService =
        retrofit.create(CardPlayerService::class.java)


    @Provides
    @Singleton
    fun provideForgotPasswordService( retrofit: Retrofit) : ForgotPasswordService =
        retrofit.create(ForgotPasswordService::class.java)

    @Provides
    @Singleton
    fun provideProfileService( retrofit : Retrofit) : ProfileService =
        retrofit.create(ProfileService::class.java)

    @Provides
    @Singleton
    fun provideHomeService(retrofit: Retrofit) : HomeService =
        retrofit.create(HomeService::class.java)

}
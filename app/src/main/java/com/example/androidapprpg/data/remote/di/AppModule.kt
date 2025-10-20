package com.example.androidapprpg.data.remote.di

import android.util.Log
import com.example.androidapprpg.BuildConfig
import com.example.androidapprpg.data.remote.authInterceptor.AuthInterceptor
import com.example.androidapprpg.data.remote.services.*
import com.example.androidapprpg.data.remote.services.spinners.*
import com.example.androidapprpg.data.repository.*
import com.example.androidapprpg.utils.websocket.StompChatSocket
import com.example.androidapprpg.webClient.services.RegisterService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // -------- LOG ----------
    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    // -------- Auth ----------
    @Provides @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor =
        AuthInterceptor(sessionManager)

    // -------- OKHTTP (API) ----------
    @Provides @Singleton @Named("apiClient")
    fun provideApiOkHttp(
        logging: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // -------- RETROFIT (API) ----------
    @Provides @Singleton @Named("api")
    fun provideRetrofit(@Named("apiClient") okHttpClient: OkHttpClient): Retrofit {
        // LOGS para cravar qual base está sendo usada
        Log.d("DI", "BuildConfig.BASE_URL_GAME=${BuildConfig.BASE_URL_GAME}")

        val rf = Retrofit.Builder()

            .baseUrl(BuildConfig.BASE_URL_GAME)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d("DI", "Retrofit.baseUrl=${rf.baseUrl()}")
        return rf
    }

    // -------- SERVICES (sempre com @Named("api")) ----------
    @Provides @Singleton
    fun provideAuthService(@Named("api") retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides @Singleton
    fun provideCadastroService(@Named("api") retrofit: Retrofit): RegisterService =
        retrofit.create(RegisterService::class.java)

    @Provides @Singleton
    fun provideNewGamesService(@Named("api") retrofit: Retrofit): NewGameService =
        retrofit.create(NewGameService::class.java)

    @Provides @Singleton
    fun provideMyGamesService(@Named("api") retrofit: Retrofit): MyGamesService =
        retrofit.create(MyGamesService::class.java)

    @Provides @Singleton
    fun provideJoinGameService(@Named("api") retrofit: Retrofit): JoinGameService =
        retrofit.create(JoinGameService::class.java)

    @Provides @Singleton
    fun provideGameLobbyService(@Named("api") retrofit: Retrofit): GameLobbyService =
        retrofit.create(GameLobbyService::class.java)

    @Provides @Singleton
    fun provideCardMasterService(@Named("api") retrofit: Retrofit): CardMasterService =
        retrofit.create(CardMasterService::class.java)

    @Provides @Singleton
    fun provideCardPoderesService(@Named("api") retrofit: Retrofit): CardPoderService =
        retrofit.create(CardPoderService::class.java)

    @Provides @Singleton
    fun provideCardMagiasService(@Named("api") retrofit: Retrofit): CardMagiasService =
        retrofit.create(CardMagiasService::class.java)

    @Provides @Singleton
    fun provideCardPlayerService(@Named("api") retrofit: Retrofit): CardPlayerService =
        retrofit.create(CardPlayerService::class.java)

    @Provides @Singleton
    fun provideForgotPasswordService(@Named("api") retrofit: Retrofit): ForgotPasswordService =
        retrofit.create(ForgotPasswordService::class.java)

    @Provides @Singleton
    fun provideProfileService(@Named("api") retrofit: Retrofit): ProfileService =
        retrofit.create(ProfileService::class.java)

    @Provides @Singleton
    fun provideHomeService(@Named("api") retrofit: Retrofit): HomeService =
        retrofit.create(HomeService::class.java)

    @Provides @Singleton
    fun provideAgenteService(@Named("api") retrofit: Retrofit): AgenteService =
        retrofit.create(AgenteService::class.java)

    @Provides @Singleton
    fun provideNotesService(@Named("api") retrofit: Retrofit): NotesService =
        retrofit.create(NotesService::class.java)

    @Provides @Singleton
    fun provideNotesRepository(service: NotesService): NotesRepository =
        NotesRepositoryImpl(service)

    @Provides @Singleton
    fun provideMapService(@Named("api") retrofit: Retrofit): MapService =
        retrofit.create(MapService::class.java)


    @Provides @Singleton @Named("gameBaseUrl")
    fun provideGameBaseUrl(): String = BuildConfig.BASE_URL_GAME

    @Provides @Singleton
    fun provideMapRepository(service: MapService, @ApplicationContext appContext: android.content.Context, @Named("gameBaseUrl") baseUrl: String): MapRepository = MapRepositoryImpl(
        service = service,
        appContext = appContext,
        baseUrl = baseUrl
    )

    // ==================== WEBSOCKET / STOMP ====================
    @Provides @Singleton fun provideGson(): Gson = Gson()

    @Provides @Singleton @Named("ws")
    fun provideWsOkHttp(): OkHttpClient {
        val cookieMgr = CookieManager().apply { setCookiePolicy(CookiePolicy.ACCEPT_ALL) }
        return OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieMgr)) // JSESSIONID
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(0, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton @Named("wsBase")
    fun provideWsBase(): String = "http://alob-rpg-958777443.sa-east-1.elb.amazonaws.com"

    @Provides @Singleton @Named("wsStage")
    fun provideWsStage(): String? = null

    @Provides @Singleton @Named("wsEndpoint")
    fun provideWsEndpoint(): String = "ws"

    @Provides @Singleton @Named("wsSockJs")
    fun provideWsSockJs(): Boolean = true

    @Provides @Singleton
    fun provideStompChatSocket(
        @Named("ws") wsClient: OkHttpClient,
        gson: Gson,
        @Named("wsBase") base: String,
        @Named("wsStage") stage: String?,
        @Named("wsEndpoint") endpoint: String,
        @Named("wsSockJs") sockJs: Boolean
    ): StompChatSocket = StompChatSocket(wsClient, gson, base, stage, endpoint, sockJs).apply {
        setAuth(emptyMap())
    }
}

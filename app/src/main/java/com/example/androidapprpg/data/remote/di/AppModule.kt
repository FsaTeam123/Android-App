package com.example.androidapprpg.data.remote.di

import com.example.androidapprpg.BuildConfig
import com.example.androidapprpg.data.remote.authInterceptor.AuthInterceptor
import com.example.androidapprpg.data.remote.services.AuthService
import com.example.androidapprpg.data.remote.services.FakeMapService
import com.example.androidapprpg.data.remote.services.FakeNotesService
import com.example.androidapprpg.data.remote.services.ForgotPasswordService
import com.example.androidapprpg.data.remote.services.GameLobbyService
import com.example.androidapprpg.data.remote.services.HomeService
import com.example.androidapprpg.data.remote.services.JoinGameService
import com.example.androidapprpg.data.remote.services.MapService
import com.example.androidapprpg.data.remote.services.MyGamesService
import com.example.androidapprpg.data.remote.services.NewGameService
import com.example.androidapprpg.data.remote.services.NotesService
import com.example.androidapprpg.data.remote.services.ProfileService
import com.example.androidapprpg.data.remote.services.spinners.CardMagiasService
import com.example.androidapprpg.data.remote.services.spinners.CardMasterService
import com.example.androidapprpg.data.remote.services.spinners.CardPlayerService
import com.example.androidapprpg.data.remote.services.spinners.CardPoderService
import com.example.androidapprpg.data.repository.MapRepository
import com.example.androidapprpg.data.repository.MapRepositoryImpl
import com.example.androidapprpg.data.repository.NotesRepository
import com.example.androidapprpg.data.repository.NotesRepositoryImpl
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.utils.websocket.ChatSocket
import com.example.androidapprpg.utils.websocket.StompChatSocket
import com.example.androidapprpg.webClient.services.RegisterService
import com.example.androidapprpg.utils.websocket.WsEventListener
import okhttp3.HttpUrl.Companion.toHttpUrl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
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

    @Provides
    @Singleton
    fun provideNotesService(): NotesService = FakeNotesService()

    //--------------------ATUALIZAR QUANDO A API ESTIVER PRONTA-------------------//

    //atualizar o NOTAS
    @Provides
    @Singleton
    fun provideNotesRepository(service: NotesService): NotesRepository =
        NotesRepositoryImpl(service)

    @Provides
    @Singleton
    fun provideMapService() : MapService = FakeMapService()

    @Provides
    @Singleton
    fun provideMapRepository(service: MapService): MapRepository =
        MapRepositoryImpl(service)


    // ---------------------- WEB SOCKET ---------------------- //
    /**
     * Cliente OkHttp específico para WebSocket:
     * - readTimeout(0) para WS
     * - pingInterval para manter a conexão
     * Mantém os MESMOS interceptors (Auth + Logging) do cliente REST,
     * pois clonamos a instância base.
     */

    // ---------------------- WEB SOCKET ---------------------- //
    @Provides @Singleton fun provideGson(): Gson = Gson()

    @Provides @Singleton @Named("ws")
    fun provideWsOkHttp(
        base: OkHttpClient,
        @Named("wsBase") baseHttps: String
    ): OkHttpClient =
        base.newBuilder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
            .pingInterval(25, java.util.concurrent.TimeUnit.SECONDS)
            .eventListenerFactory { WsEventListener(baseHttps.toHttpUrl().host) } // 👈 LOG DNS/IP do ALB
            .build()

    // Sempre com HTTP para virar ws:// no socket:
    @Provides @Singleton @Named("wsBase")
    fun provideWsBase(): String =
        "http://alob-rpg-958777443.sa-east-1.elb.amazonaws.com"

    // COMO VOCÊ TEM UM STAGE "ws" NO BALANCEADOR/ROTEADOR:
    @Provides @Singleton @Named("wsStage")
    fun provideWsStage(): String? = "ws"

    // Para não duplicar /ws/ws, deixe o endpoint vazio:
    @Provides @Singleton @Named("wsEndpoint")
    fun provideWsEndpoint(): String = ""    // <<<< importante

    // Use true se o backend for SockJS (.withSockJS()) e precisar de /websocket:
    @Provides @Singleton @Named("wsSockJs")
    fun provideWsSockJs(): Boolean = false  // mude para true se necessário

    @Provides @Singleton
    fun provideStompChatSocket(
        @Named("ws") wsClient: OkHttpClient,
        gson: Gson,
        @Named("wsBase") baseHttps: String,
        @Named("wsStage") stage: String?,
        @Named("wsEndpoint") endpoint: String,
        @Named("wsSockJs") sockJs: Boolean
    ): StompChatSocket = StompChatSocket(wsClient, gson, baseHttps, stage, endpoint, sockJs)

    @Provides @Singleton
    fun provideChatSocket(impl: StompChatSocket): ChatSocket = impl
}
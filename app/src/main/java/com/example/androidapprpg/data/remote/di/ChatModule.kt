package com.example.androidapprpg.data.remote.di

import com.example.androidapprpg.adapter.ChatSocketStompAdapter
import com.example.androidapprpg.utils.websocket.ChatSocket
import com.example.androidapprpg.utils.websocket.StompChatSocketImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {
    @Binds
    @Singleton
    abstract fun bindChatSocket(impl: StompChatSocketImpl): ChatSocket
}

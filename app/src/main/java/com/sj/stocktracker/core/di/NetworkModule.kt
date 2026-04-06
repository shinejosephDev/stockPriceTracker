package com.sj.stocktracker.core.di

import com.sj.stocktracker.data.network.IWebSocketManager
import com.sj.stocktracker.data.network.WebSocketManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindWebSocketManager(impl: WebSocketManager): IWebSocketManager
}

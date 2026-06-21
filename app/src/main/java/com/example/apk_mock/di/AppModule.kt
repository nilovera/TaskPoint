package com.example.apk_mock.di

import android.content.Context
import com.example.apk_mock.data.local.OfferCatalogImporter
import com.example.apk_mock.data.local.TaskPointDatabase
import com.example.apk_mock.data.remote.AuthApi
import com.example.apk_mock.data.remote.RetrofitClient
import com.example.apk_mock.data.repository.RemoteAuthRepository
import com.example.apk_mock.data.repository.RoomCategoriaRepository
import com.example.apk_mock.data.repository.RoomOfferRepository
import com.example.apk_mock.data.repository.RoomRutinaRepository
import com.example.apk_mock.data.repository.RoomTareaRepository
import com.example.apk_mock.data.secure.SecureSessionStorage
import com.example.apk_mock.data.source.TaskPhotoStorage
import com.example.apk_mock.domain.repository.AuthRepository
import com.example.apk_mock.domain.repository.CategoriaRepository
import com.example.apk_mock.domain.repository.OfferRepository
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.TareaRepository
import com.example.apk_mock.domain.repository.UserSessionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskPointDatabase {
        return TaskPointDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRetrofitClient(): RetrofitClient = RetrofitClient()

    @Provides
    fun provideAuthApi(retrofitClient: RetrofitClient): AuthApi = retrofitClient.authApi

    @Provides
    @Singleton
    fun provideSecureSessionStorage(@ApplicationContext context: Context): SecureSessionStorage {
        return SecureSessionStorage(context)
    }

    @Provides
    @Singleton
    fun provideTaskPhotoStorage(@ApplicationContext context: Context): TaskPhotoStorage {
        return TaskPhotoStorage(context)
    }

    @Provides
    @Singleton
    fun provideOfferCatalogImporter(
        @ApplicationContext context: Context,
        database: TaskPointDatabase
    ): OfferCatalogImporter = OfferCatalogImporter(context, database)

    @Provides
    @Singleton
    fun provideRemoteAuthRepository(
        authApi: AuthApi,
        sessionStorage: SecureSessionStorage
    ): RemoteAuthRepository = RemoteAuthRepository(authApi, sessionStorage)

    @Provides
    fun provideAuthRepository(repository: RemoteAuthRepository): AuthRepository = repository

    @Provides
    fun provideUserSessionProvider(repository: RemoteAuthRepository): UserSessionProvider = repository

    @Provides
    @Singleton
    fun provideTareaRepository(
        database: TaskPointDatabase,
        sessionProvider: UserSessionProvider,
        photoStorage: TaskPhotoStorage
    ): TareaRepository = RoomTareaRepository(database, sessionProvider, photoStorage)

    @Provides
    @Singleton
    fun provideRutinaRepository(
        database: TaskPointDatabase,
        sessionProvider: UserSessionProvider
    ): RutinaRepository = RoomRutinaRepository(database, sessionProvider)

    @Provides
    @Singleton
    fun provideCategoriaRepository(
        database: TaskPointDatabase,
        offerCatalogImporter: OfferCatalogImporter
    ): CategoriaRepository = RoomCategoriaRepository(database.categoriaDao(), offerCatalogImporter)

    @Provides
    @Singleton
    fun provideOfferRepository(
        database: TaskPointDatabase,
        offerCatalogImporter: OfferCatalogImporter
    ): OfferRepository = RoomOfferRepository(database.offerDao(), offerCatalogImporter)
}

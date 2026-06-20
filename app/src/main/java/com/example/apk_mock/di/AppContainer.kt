package com.example.apk_mock.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.apk_mock.data.local.OfferCatalogImporter
import com.example.apk_mock.data.local.TaskPointDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer private constructor(context: Context) {
    private val applicationContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val taskPhotoStorage = TaskPhotoStorage(applicationContext)
    private val secureSessionStorage = SecureSessionStorage(applicationContext)
    val database: TaskPointDatabase = TaskPointDatabase.getInstance(applicationContext)
    val retrofitClient: RetrofitClient = RetrofitClient()
    private val offerCatalogImporter = OfferCatalogImporter(applicationContext, database)
    private val sessionRepository = RemoteAuthRepository(
        retrofitClient.authApi,
        secureSessionStorage
    )

    val authRepository: AuthRepository = sessionRepository
    val userSessionProvider: UserSessionProvider = sessionRepository
    val rutinaRepository: RutinaRepository = RoomRutinaRepository(
        database,
        sessionRepository
    )
    val tareaRepository: TareaRepository = RoomTareaRepository(
        database,
        sessionRepository,
        taskPhotoStorage
    )
    val categoriaRepository: CategoriaRepository = RoomCategoriaRepository(
        database.categoriaDao(),
        offerCatalogImporter
    )
    val offerRepository: OfferRepository = RoomOfferRepository(
        database.offerDao(),
        offerCatalogImporter
    )

    init {
        applicationScope.launch {
            offerCatalogImporter.importIfNeeded()
        }
    }

    companion object {
        fun create(context: Context): AppContainer {
            return AppContainer(context.applicationContext)
        }
    }
}

@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current.applicationContext
    return remember(context) { AppContainer.create(context) }
}

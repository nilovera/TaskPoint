package com.example.apk_mock.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.apk_mock.data.local.dao.CategoriaDao
import com.example.apk_mock.data.local.dao.OfferDao
import com.example.apk_mock.data.local.dao.RutinaDao
import com.example.apk_mock.data.local.dao.SyncOperationDao
import com.example.apk_mock.data.local.dao.TareaDao
import com.example.apk_mock.data.local.entity.CategoriaEntity
import com.example.apk_mock.data.local.entity.OfferEntity
import com.example.apk_mock.data.local.entity.RutinaEntity
import com.example.apk_mock.data.local.entity.StoreEntity
import com.example.apk_mock.data.local.entity.SyncOperationEntity
import com.example.apk_mock.data.local.entity.TareaEntity

@Database(
    entities = [
        CategoriaEntity::class,
        OfferEntity::class,
        RutinaEntity::class,
        StoreEntity::class,
        SyncOperationEntity::class,
        TareaEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class TaskPointDatabase : RoomDatabase() {
    abstract fun categoriaDao(): CategoriaDao
    abstract fun offerDao(): OfferDao
    abstract fun rutinaDao(): RutinaDao
    abstract fun syncOperationDao(): SyncOperationDao
    abstract fun tareaDao(): TareaDao

    companion object {
        private const val DATABASE_NAME = "taskpoint.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_operations` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `entityType` TEXT NOT NULL,
                        `entityId` TEXT NOT NULL,
                        `operationType` TEXT NOT NULL,
                        `payloadJson` TEXT,
                        `status` TEXT NOT NULL,
                        `attempts` INTEGER NOT NULL,
                        `lastError` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_operations_userId` ON `sync_operations` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_operations_entityType_entityId` ON `sync_operations` (`entityType`, `entityId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_operations_status` ON `sync_operations` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_operations_createdAt` ON `sync_operations` (`createdAt`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `rutinas` ADD COLUMN `latitude` REAL")
                db.execSQL("ALTER TABLE `rutinas` ADD COLUMN `longitude` REAL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `tareas` ADD COLUMN `requiereRevisionHorario` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        @Volatile
        private var instance: TaskPointDatabase? = null

        fun getInstance(context: Context): TaskPointDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskPointDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
        }
    }
}

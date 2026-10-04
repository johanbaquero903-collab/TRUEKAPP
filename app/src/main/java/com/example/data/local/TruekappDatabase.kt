package com.example.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ExchangeEntity
import com.example.data.model.ListingEntity
import com.example.data.model.ReportedUserEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserReviewEntity

@Database(
    entities = [
        UserEntity::class,
        ListingEntity::class,
        ExchangeEntity::class,
        ChatMessageEntity::class,
        UserReviewEntity::class,
        ReportedUserEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TruekappDatabase : RoomDatabase() {

    abstract fun truekappDao(): TruekappDao

    companion object {
        const val DB_NAME = "truekapp_database"
        const val EXPECTED_IDENTITY_HASH = "f0718d4047bd6ebd53b78aa6cd2297a2"

        @Volatile
        private var INSTANCE: TruekappDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `users` ADD COLUMN `password` TEXT NOT NULL DEFAULT '123456'")
                } catch (_: Exception) {
                    // Column may already exist
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schema is unchanged between v2 and v3; preserves existing demo data
            }
        }

        fun getDatabase(context: Context): TruekappDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                ensureDatabaseIntegrity(appContext)
                val instance = Room.databaseBuilder(
                    appContext,
                    TruekappDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Verifies that any pre-existing SQLite file on disk matches Room's expected
         * identity hash before Room opens it. If an older build left a mismatched
         * schema hash on the same database version, deletes the stale database file
         * so Room can recreate and re-seed it cleanly without throwing IllegalStateException.
         */
        private fun ensureDatabaseIntegrity(context: Context) {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) return

            var sqliteDb: SQLiteDatabase? = null
            var shouldReset = false
            try {
                sqliteDb = SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
                var storedHash: String? = null
                sqliteDb.rawQuery(
                    "SELECT identity_hash FROM room_master_table LIMIT 1",
                    null
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        storedHash = cursor.getString(0)
                    }
                }
                if (storedHash != EXPECTED_IDENTITY_HASH) {
                    shouldReset = true
                }
            } catch (_: Exception) {
                shouldReset = true
            } finally {
                try {
                    sqliteDb?.close()
                } catch (_: Exception) {
                }
            }

            if (shouldReset) {
                context.deleteDatabase(DB_NAME)
            }
        }

        internal fun resetInstanceForTesting() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}

package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 2,
    exportSchema = false
)
abstract class TruekappDatabase : RoomDatabase() {

    abstract fun truekappDao(): TruekappDao

    companion object {
        @Volatile
        private var INSTANCE: TruekappDatabase? = null

        fun getDatabase(context: Context): TruekappDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TruekappDatabase::class.java,
                    "truekapp_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

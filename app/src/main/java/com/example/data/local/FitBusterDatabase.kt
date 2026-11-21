package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserProfileEntity::class,
        WorkoutSessionEntity::class,
        SprintRecordEntity::class,
        RestLogEntity::class,
        MealEntryEntity::class,
        WaterLogEntity::class,
        MissionProgressEntity::class,
        MedalRecordEntity::class,
        CertificateRecordEntity::class,
        DisciplineLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(FitBusterTypeConverters::class)
abstract class FitBusterDatabase : RoomDatabase() {
    abstract fun fitBusterDao(): FitBusterDao

    companion object {
        @Volatile
        private var INSTANCE: FitBusterDatabase? = null

        fun getInstance(context: Context): FitBusterDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FitBusterDatabase::class.java,
                    "fit_buster.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

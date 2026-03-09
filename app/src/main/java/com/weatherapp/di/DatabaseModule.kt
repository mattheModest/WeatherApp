package com.weatherapp.di

import android.content.Context
import androidx.room.Room
import com.weatherapp.data.db.AppDatabase
import com.weatherapp.data.db.dao.AlertStateDao
import com.weatherapp.data.db.dao.ForecastDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "weather_app_db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .build()

    @Provides
    fun provideForecastDao(db: AppDatabase): ForecastDao = db.forecastDao()

    @Provides
    fun provideAlertStateDao(db: AppDatabase): AlertStateDao = db.alertStateDao()
}

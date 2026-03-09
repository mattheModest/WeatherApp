package com.weatherapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.WorkManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.weatherapp.data.billing.BillingClientWrapper
import com.weatherapp.data.calendar.CalendarRepository
import com.weatherapp.data.datastore.weatherDataStore
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
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.weatherDataStore

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideBillingClient(
        @ApplicationContext context: Context,
        billingClientWrapperProvider: dagger.Lazy<BillingClientWrapper>
    ): BillingClient {
        val listener = PurchasesUpdatedListener { result: BillingResult, purchases: List<Purchase>? ->
            billingClientWrapperProvider.get().notifyPurchasesUpdated(result, purchases)
        }
        return BillingClient.newBuilder(context)
            .setListener(listener)
            .enablePendingPurchases()
            .build()
    }

    @Provides
    @Singleton
    fun provideCalendarRepository(
        @ApplicationContext context: Context
    ): CalendarRepository = CalendarRepository(context)
}

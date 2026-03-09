package com.weatherapp.data.billing

import android.app.Activity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.android.billingclient.api.BillingResponseCode
import com.android.billingclient.api.Purchase
import com.weatherapp.data.datastore.PreferenceKeys
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val billingClientWrapper: BillingClientWrapper,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val PREMIUM_SKU = "weatherapp_premium_annual"
    }

    init {
        billingClientWrapper.setPurchasesUpdatedCallback { result, purchases ->
            if (result.responseCode == BillingResponseCode.OK && purchases != null) {
                purchases.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    it.products.contains(PREMIUM_SKU)
                }.forEach { purchase ->
                    if (!purchase.isAcknowledged) {
                        // Acknowledgment handled by checkAndUpdatePremiumStatus on next cycle
                        Timber.d("BillingRepository: purchase received, pending acknowledgment")
                    }
                }
            }
        }
    }

    suspend fun checkAndUpdatePremiumStatus(): Result<Boolean> = runCatching {
        if (!billingClientWrapper.ensureConnected()) {
            Timber.w("Billing unavailable — reading cached isPremium")
            return@runCatching dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false
        }

        val purchases = billingClientWrapper.querySubscriptionPurchases()
        val isActive = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
            purchase.products.contains(PREMIUM_SKU)
        }

        // Acknowledge any unacknowledged purchases
        purchases.filter {
            it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
        }.forEach { purchase ->
            val ackResult = billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
            if (ackResult.responseCode == BillingResponseCode.OK) {
                Timber.d("BillingRepository: purchase acknowledged")
            } else {
                Timber.w("BillingRepository: acknowledgment failed: ${ackResult.responseCode}")
            }
        }

        val nowEpoch = System.currentTimeMillis() / 1000L
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.KEY_IS_PREMIUM] = isActive
            prefs[PreferenceKeys.KEY_LAST_BILLING_CHECK] = nowEpoch
        }
        Timber.d("BillingRepository: isPremium=$isActive")
        isActive
    }

    suspend fun launchPurchaseFlow(activity: Activity): Result<Unit> = runCatching {
        val productDetails = billingClientWrapper.queryProductDetails(PREMIUM_SKU)
            ?: run {
                Timber.w("BillingRepository: product details unavailable for $PREMIUM_SKU")
                return@runCatching
            }
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()?.offerToken
            ?: run {
                Timber.w("BillingRepository: no offer token found")
                return@runCatching
            }
        val result = billingClientWrapper.launchBillingFlow(activity, productDetails, offerToken)
        Timber.d("BillingRepository: launchBillingFlow result=${result.responseCode}")
    }
}

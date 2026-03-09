package com.weatherapp.data.billing

import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingClientWrapper @Inject constructor(
    private val billingClient: BillingClient
) {
    private var purchasesUpdatedCallback: ((BillingResult, List<Purchase>?) -> Unit)? = null

    fun setPurchasesUpdatedCallback(callback: (BillingResult, List<Purchase>?) -> Unit) {
        purchasesUpdatedCallback = callback
    }

    fun notifyPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        purchasesUpdatedCallback?.invoke(result, purchases)
    }

    suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    val connected = result.responseCode == BillingResponseCode.OK
                    Timber.d("BillingClientWrapper: setup finished, connected=$connected")
                    if (continuation.isActive) continuation.resume(connected)
                }
                override fun onBillingServiceDisconnected() {
                    Timber.w("BillingClientWrapper: service disconnected")
                    if (continuation.isActive) continuation.resume(false)
                }
            })
        }
    }

    suspend fun querySubscriptionPurchases(): List<Purchase> {
        if (!ensureConnected()) return emptyList()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = billingClient.queryPurchasesAsync(params)
        return if (result.billingResult.responseCode == BillingResponseCode.OK) {
            result.purchasesList
        } else {
            Timber.w("queryPurchasesAsync failed: ${result.billingResult.responseCode}")
            emptyList()
        }
    }

    suspend fun queryProductDetails(sku: String): ProductDetails? {
        if (!ensureConnected()) return null
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        val result = billingClient.queryProductDetails(params)
        return if (result.billingResult.responseCode == BillingResponseCode.OK) {
            result.productDetailsList?.firstOrNull()
        } else {
            Timber.w("queryProductDetails failed: ${result.billingResult.responseCode}")
            null
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails, offerToken: String): BillingResult {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        return billingClient.launchBillingFlow(activity, params)
    }

    suspend fun acknowledgePurchase(purchaseToken: String): BillingResult {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        return billingClient.acknowledgePurchase(params)
    }

    fun close() {
        billingClient.endConnection()
    }
}

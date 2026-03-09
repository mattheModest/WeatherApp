package com.weatherapp.data.billing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import com.weatherapp.data.datastore.PreferenceKeys
import androidx.datastore.preferences.core.edit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BillingRepositoryTest {

    private lateinit var billingClientWrapper: BillingClientWrapper
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var billingRepository: BillingRepository

    @Before
    fun setUp() {
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        billingClientWrapper = mockk(relaxed = true)
        dataStore = mockk()
        billingRepository = BillingRepository(billingClientWrapper, dataStore)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun billingConnected_activeSubscription_writesPremiumTrue_andUpdatesLastBillingCheck() = runTest {
        // Billing connected
        coEvery { billingClientWrapper.ensureConnected() } returns true

        // Active subscription purchase
        val purchase = mockk<com.android.billingclient.api.Purchase>(relaxed = true) {
            every { purchaseState } returns com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
            every { products } returns listOf(BillingRepository.PREMIUM_SKU)
            every { isAcknowledged } returns true
        }
        coEvery { billingClientWrapper.querySubscriptionPurchases() } returns listOf(purchase)

        // DataStore capture
        val editSlot = slot<suspend (MutablePreferences) -> Unit>()
        val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(capture(editSlot)) } coAnswers {
            editSlot.captured.invoke(mutablePrefs)
            mutablePrefs
        }

        val result = billingRepository.checkAndUpdatePremiumStatus()

        assertTrue("Result should be success", result.isSuccess)
        assertTrue("isPremium should be true", result.getOrNull() == true)
        coVerify { mutablePrefs[PreferenceKeys.KEY_IS_PREMIUM] = true }
        coVerify { mutablePrefs[PreferenceKeys.KEY_LAST_BILLING_CHECK] = any() }
    }

    @Test
    fun billingConnectionFails_readsCachedIsPremium_doesNotWriteFalse() = runTest {
        // Billing cannot connect
        coEvery { billingClientWrapper.ensureConnected() } returns false

        // Cached isPremium = true
        val prefs = mockk<Preferences> {
            every { this@mockk[PreferenceKeys.KEY_IS_PREMIUM] } returns true
        }
        every { dataStore.data } returns flowOf(prefs)

        val result = billingRepository.checkAndUpdatePremiumStatus()

        assertTrue("Result should be success (read from cache)", result.isSuccess)
        assertEquals("Should read cached value = true", true, result.getOrNull())

        // Should NOT call dataStore.edit (no writes)
        coVerify(exactly = 0) { dataStore.edit(any()) }
    }

    @Test
    fun noActiveSubscription_writesPremiumFalse() = runTest {
        // Billing connected
        coEvery { billingClientWrapper.ensureConnected() } returns true

        // No active purchases
        coEvery { billingClientWrapper.querySubscriptionPurchases() } returns emptyList()

        // DataStore capture
        val editSlot = slot<suspend (MutablePreferences) -> Unit>()
        val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(capture(editSlot)) } coAnswers {
            editSlot.captured.invoke(mutablePrefs)
            mutablePrefs
        }

        val result = billingRepository.checkAndUpdatePremiumStatus()

        assertTrue("Result should be success", result.isSuccess)
        assertFalse("isPremium should be false", result.getOrNull() == true)
        coVerify { mutablePrefs[PreferenceKeys.KEY_IS_PREMIUM] = false }
    }
}

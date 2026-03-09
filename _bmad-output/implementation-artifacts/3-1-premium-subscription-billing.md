# Story 3.1: Premium Subscription & Billing

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a user,
I want to subscribe to WeatherApp Premium and have the app immediately unlock calendar-powered features,
so that I can access proactive event intelligence without any delay after subscribing.

## Acceptance Criteria

**AC-1:** Given `BillingRepository` and `BillingClientWrapper` when a successful subscription purchase completes via Google Play Billing 7.1.1 then `DataStore[KEY_IS_PREMIUM]` is set to `true` and `DataStore[KEY_LAST_BILLING_CHECK]` is updated with the current epoch.

**AC-2:** Given `DataStore[KEY_IS_PREMIUM]` is `true` when `ForecastRefreshWorker` starts its cycle then it reads `isPremium` from DataStore at the very start of `doWork()` and gates all premium paths (CalendarScanWorker enqueue) behind this check.

**AC-3:** Given `DataStore[KEY_IS_PREMIUM]` is `false` when `ForecastRefreshWorker` runs then `CalendarScanWorker` is not enqueued; no calendar queries are made; no premium UI appears on the widget.

**AC-4:** Given the Google Play Billing client connection fails when `BillingRepository` attempts to verify subscription state then it reads the last cached `DataStore[KEY_IS_PREMIUM]` value without crashing; it does not revert premium status on a transient connection failure.

**AC-5:** Given the premium upgrade entry point in `SettingsScreen` when the user taps it then the Google Play in-app purchase flow is launched for the $7.99/year subscription SKU.

**AC-6:** Given the user's subscription lapses or is cancelled when `BillingRepository` detects this on the next billing check then `DataStore[KEY_IS_PREMIUM]` is set to `false` and the widget reverts to free-tier display on the next WorkManager cycle.

## Tasks / Subtasks

- [ ] Task 1: Create `BillingClientWrapper.kt` (AC: 1, 4, 5)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/data/billing/BillingClientWrapper.kt`
  - [ ] 1.2 Wrap `BillingClient` lifecycle: `startConnection()` in a suspending function with a `CompletableDeferred` callback; handle `onBillingSetupFinished` and `onBillingServiceDisconnected`
  - [ ] 1.3 Implement `suspend fun isConnected(): Boolean` — retries connection if disconnected, returns false after 3 attempts
  - [ ] 1.4 Implement `suspend fun querySubscriptionPurchases(): List<Purchase>` — queries `BillingClient.ProductType.SUBS`
  - [ ] 1.5 Implement `fun launchBillingFlow(activity: Activity, productDetails: ProductDetails, offerToken: String)` — wraps `BillingClient.launchBillingFlow()`
  - [ ] 1.6 Handle `BillingResponseCode.SERVICE_UNAVAILABLE` and `NETWORK_ERROR` gracefully — return empty list, do not throw
  - [ ] 1.7 Close `BillingClient` connection when no longer needed (call from `BillingRepository.close()`)

- [ ] Task 2: Create `BillingRepository.kt` (AC: 1, 4, 5, 6)
  - [ ] 2.1 Create `app/src/main/java/com/weatherapp/data/billing/BillingRepository.kt`
  - [ ] 2.2 Inject `BillingClientWrapper` and `DataStore<Preferences>` via Hilt
  - [ ] 2.3 Define SKU constant: `const val PREMIUM_SKU = "weatherapp_premium_annual"` (placeholder; actual SKU from Play Console)
  - [ ] 2.4 Implement `suspend fun checkAndUpdatePremiumStatus(): Result<Boolean>` using `runCatching { ... }`:
    - If `BillingClientWrapper.isConnected()` → query purchases → check for active `PREMIUM_SKU` subscription
    - If active subscription found → write `KEY_IS_PREMIUM = true`, `KEY_LAST_BILLING_CHECK = nowEpoch`, return `Result.success(true)`
    - If no active subscription → write `KEY_IS_PREMIUM = false`, return `Result.success(false)`
    - If connection fails → `Timber.w("Billing connection failed — using cached isPremium")`, read `KEY_IS_PREMIUM` from DataStore, return `Result.success(cachedValue)` — NEVER revert on connection failure
  - [ ] 2.5 Implement `suspend fun launchPurchaseFlow(activity: Activity): Result<Unit>`:
    - Query `ProductDetails` for `PREMIUM_SKU`
    - Launch billing flow
    - Purchase result arrives via `BillingClientWrapper.purchasesUpdatedListener`
  - [ ] 2.6 Implement `fun observePurchaseUpdates(): Flow<PurchasesResult>` for real-time purchase updates

- [ ] Task 3: Handle purchase acknowledgment (AC: 1)
  - [ ] 3.1 Google Play requires purchases to be acknowledged within 3 days or they are refunded
  - [ ] 3.2 In `BillingClientWrapper.purchasesUpdatedListener`, when `responseCode == BillingResponseCode.OK`:
    - For each `Purchase` with `purchaseState == Purchase.PurchaseState.PURCHASED` and `!isAcknowledged`
    - Call `BillingClient.acknowledgePurchase()` with an `AcknowledgePurchaseParams`
  - [ ] 3.3 After acknowledgment → write `KEY_IS_PREMIUM = true` to DataStore via `BillingRepository`

- [ ] Task 4: Create Hilt module for BillingClient (AC: 1)
  - [ ] 4.1 Add to `app/src/main/java/com/weatherapp/di/AppModule.kt`:
    ```kotlin
    @Provides @Singleton
    fun provideBillingClient(@ApplicationContext context: Context): BillingClient =
        BillingClient.newBuilder(context)
            .setListener { responseCode, purchases -> /* handled in BillingClientWrapper */ }
            .enablePendingPurchases()
            .build()

    @Provides @Singleton
    fun provideBillingClientWrapper(billingClient: BillingClient): BillingClientWrapper =
        BillingClientWrapper(billingClient)

    @Provides @Singleton
    fun provideBillingRepository(
        wrapper: BillingClientWrapper,
        dataStore: DataStore<Preferences>
    ): BillingRepository = BillingRepository(wrapper, dataStore)
    ```

- [ ] Task 5: Wire premium check at start of `ForecastRefreshWorker.doWork()` (AC: 2, 3)
  - [ ] 5.1 Inject `BillingRepository` into `ForecastRefreshWorker` via `@AssistedInject`
  - [ ] 5.2 At the very FIRST line of `doWork()`, read `isPremium`:
    ```kotlin
    val isPremium = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false
    Timber.d("ForecastRefreshWorker: isPremium=$isPremium")
    ```
  - [ ] 5.3 After `WeatherWidget.update()` and `AlertEvaluationWorker` enqueue, add:
    ```kotlin
    if (isPremium) {
        val calendarWork = OneTimeWorkRequestBuilder<CalendarScanWorker>().build()
        workManager.enqueue(calendarWork)
        Timber.d("CalendarScanWorker enqueued for premium user")
    }
    ```
  - [ ] 5.4 `CalendarScanWorker` is NEVER enqueued when `isPremium = false`

- [ ] Task 6: Wire premium purchase flow from `SettingsScreen` (AC: 5)
  - [ ] 6.1 In `SettingsViewModel.kt`, add `fun onUpgradeTapped(activity: Activity)` calling `billingRepository.launchPurchaseFlow(activity)`
  - [ ] 6.2 In `SettingsScreen.kt`, the premium upgrade `OutlinedButton` triggers `viewModel.onUpgradeTapped(activity)`
  - [ ] 6.3 The `Activity` reference is obtained from `LocalContext.current as Activity` in the composable (acceptable for launching Play Billing flow)
  - [ ] 6.4 After purchase is confirmed (via purchase update listener), `KEY_IS_PREMIUM = true` is written and `SettingsViewModel.uiState` will automatically reflect the updated `isPremium = true` state via the DataStore Flow

- [ ] Task 7: Subscription lapse detection (AC: 6)
  - [ ] 7.1 `BillingRepository.checkAndUpdatePremiumStatus()` is called periodically — add this to `ForecastRefreshWorker.doWork()` once every 24 hours (check `KEY_LAST_BILLING_CHECK`)
  - [ ] 7.2 If more than 24 hours since `KEY_LAST_BILLING_CHECK`, call `checkAndUpdatePremiumStatus()` to refresh subscription state from Google Play
  - [ ] 7.3 If subscription lapses: `KEY_IS_PREMIUM = false` → next `ForecastRefreshWorker` cycle will NOT enqueue `CalendarScanWorker` → widget reverts to free-tier display naturally

- [ ] Task 8: Write unit tests for `BillingRepository` (AC: 1, 4, 6)
  - [ ] 8.1 Create `app/src/test/java/com/weatherapp/data/billing/BillingRepositoryTest.kt`
  - [ ] 8.2 Test: successful purchase → `KEY_IS_PREMIUM = true`, `KEY_LAST_BILLING_CHECK` updated
  - [ ] 8.3 Test: billing connection failure → reads cached `KEY_IS_PREMIUM` without crash; does NOT set to false
  - [ ] 8.4 Test: no active subscription found → `KEY_IS_PREMIUM = false`
  - [ ] 8.5 Test: `isPremium` gating in `ForecastRefreshWorker` — when `KEY_IS_PREMIUM = false`, verify `CalendarScanWorker` is NOT enqueued

## Dev Notes

### Critical Architecture Rules for This Story

**1. isPremium Check — First Line of doWork()**

This is a mandatory architecture rule: "Check `isPremium` from DataStore at the start of every WorkManager cycle before executing premium paths."

```kotlin
// ForecastRefreshWorker.doWork() — first lines
override suspend fun doWork(): Result {
    val isPremium = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false
    Timber.d("doWork: isPremium=$isPremium, attempt=${runAttemptCount}")

    // Set staleness flag
    dataStore.edit { prefs -> prefs[PreferenceKeys.KEY_STALENESS_FLAG] = true }

    // ... weather fetch logic ...

    // Premium gating — AFTER free-tier work is done
    if (isPremium) {
        workManager.enqueue(OneTimeWorkRequestBuilder<CalendarScanWorker>().build())
    }

    return Result.success()
}
```

**2. BillingRepository — Never Revert on Connection Failure**

```kotlin
// CORRECT — connection failure handling
suspend fun checkAndUpdatePremiumStatus(): Result<Boolean> = runCatching {
    if (!billingClientWrapper.isConnected()) {
        Timber.w("Billing unavailable — reading cached isPremium")
        val cached = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false
        return@runCatching cached // preserve cached state; do NOT write false
    }
    // ... query purchases ...
}

// WRONG — never do this on connection failure
if (!billingClientWrapper.isConnected()) {
    dataStore.edit { prefs -> prefs[PreferenceKeys.KEY_IS_PREMIUM] = false } // ❌
}
```

**3. BillingClientWrapper — Suspending Connection Pattern**

```kotlin
// BillingClientWrapper.kt
class BillingClientWrapper(private val billingClient: BillingClient) {

    private var connected = false

    suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    connected = result.responseCode == BillingResponseCode.OK
                    continuation.resume(connected)
                }
                override fun onBillingServiceDisconnected() {
                    connected = false
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
}
```

**4. Purchase Active Check**

A subscription is active when:
- `purchase.purchaseState == Purchase.PurchaseState.PURCHASED`
- `purchase.products.contains(PREMIUM_SKU)`
- `purchase.isAcknowledged` (after first acknowledgment; unacknowledged is still valid if < 3 days)

```kotlin
private fun isActiveSubscription(purchases: List<Purchase>): Boolean {
    return purchases.any { purchase ->
        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
        purchase.products.contains(PREMIUM_SKU)
    }
}
```

**5. 24-Hour Billing Check Schedule**

```kotlin
// ForecastRefreshWorker.doWork() — billing check gate
val lastBillingCheck = dataStore.data.first()[PreferenceKeys.KEY_LAST_BILLING_CHECK] ?: 0L
val nowEpoch = System.currentTimeMillis() / 1000L
val hoursSinceLastCheck = (nowEpoch - lastBillingCheck) / 3600L

if (hoursSinceLastCheck >= 24) {
    Timber.d("Running 24-hour billing check")
    billingRepository.checkAndUpdatePremiumStatus()
    // isPremium re-read from DataStore after this
}
```

**6. Anti-Patterns to Avoid**

- NEVER revert `KEY_IS_PREMIUM` to false on a billing connection failure — only on confirmed inactive subscription
- NEVER read `isPremium` from the Billing client directly in a Glance composable — DataStore only
- NEVER enqueue `CalendarScanWorker` when `isPremium = false`
- NEVER hardcode pricing ("$7.99") in the app — use Play Console for pricing
- NEVER use `Log.*` — use `Timber.d/i/w/e()`
- NEVER run the billing check on every WorkManager cycle — throttle to once per 24 hours

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  data/billing/
    BillingClientWrapper.kt          ← NEW: BillingClient lifecycle management
    BillingRepository.kt             ← NEW: premium state management, purchase flow

app/src/test/java/com/weatherapp/
  data/billing/
    BillingRepositoryTest.kt         ← NEW
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  di/
    AppModule.kt                      ← MODIFY: add BillingClient, BillingClientWrapper, BillingRepository providers
  worker/
    ForecastRefreshWorker.kt          ← MODIFY: add isPremium check at start, CalendarScanWorker gating, 24h billing check
  ui/settings/
    SettingsViewModel.kt              ← MODIFY: add onUpgradeTapped() calling billingRepository.launchPurchaseFlow()
    SettingsScreen.kt                 ← MODIFY: wire upgrade button to viewModel.onUpgradeTapped(activity)
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `DataStore<Preferences>`, `PreferenceKeys.KEY_IS_PREMIUM`, `KEY_LAST_BILLING_CHECK`, `ForecastRefreshWorker` skeleton
- **Depends on Story 1.8**: `SettingsScreen` premium upgrade entry point (created in Story 1.8 as placeholder CTA)
- **Provides to Story 3.2**: `KEY_IS_PREMIUM = true` in DataStore gates `CalendarScanWorker` enqueue; `CalendarRepository` will be queried only when this is true
- **Provides to Story 3.3**: `ForecastRefreshWorker` now enqueues `CalendarScanWorker` when `isPremium = true`
- **Provides to Story 3.4**: `KEY_IS_PREMIUM` gating ensures premium alert evaluation only runs for premium users

### References

- [Source: architecture.md#Authentication & Security] — No custom auth; premium state via Google Play Billing
- [Source: architecture.md#Enforcement Guidelines] — Rule 9: Check isPremium at start of every WorkManager cycle
- [Source: architecture.md#Data Architecture] — `isPremium`, `lastBillingCheck` in DataStore
- [Source: epics.md#Story 3.1] — All acceptance criteria

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List

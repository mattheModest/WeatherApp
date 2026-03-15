package com.weatherapp.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.weatherapp.data.billing.BillingRepository
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.util.UiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val editSlot = slot<suspend (MutablePreferences) -> Unit>()

    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var mockBillingRepository: BillingRepository
    private lateinit var mockPrefs: Preferences
    private lateinit var mockMutablePrefs: MutablePreferences

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        mockPrefs = mockk<Preferences>(relaxed = true)
        mockMutablePrefs = mockk(relaxed = true)

        mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        mockBillingRepository = mockk(relaxed = true)
        every { mockDataStore.data } returns flowOf(mockPrefs)
        coEvery { mockDataStore.edit(capture(editSlot)) } coAnswers {
            editSlot.captured.invoke(mockMutablePrefs)
            mockPrefs
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun setupPrefs(
        tempUnit: String = "celsius",
        notificationsEnabled: Boolean = false,
        isPremium: Boolean = false,
        moodLine: String = "A really good day."
    ) {
        every { mockPrefs[PreferenceKeys.KEY_TEMP_UNIT] } returns tempUnit
        every { mockPrefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns notificationsEnabled
        every { mockPrefs[PreferenceKeys.KEY_IS_PREMIUM] } returns isPremium
        every { mockPrefs[PreferenceKeys.KEY_MOOD_LINE] } returns moodLine
        every { mockPrefs[PreferenceKeys.KEY_PERSONALITY_CORE] } returns null
    }

    @Test
    fun `loads settings from DataStore correctly`() = runTest(testDispatcher) {
        setupPrefs(
            tempUnit = "celsius",
            notificationsEnabled = true,
            isPremium = false,
            moodLine = "Grey but manageable."
        )

        val viewModel = SettingsViewModel(mockDataStore, mockBillingRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Success", state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(TempUnit.CELSIUS, data.tempUnit)
        assertTrue(data.notificationsEnabled)
        assertFalse(data.isPremium)
        assertEquals("Grey but manageable.", data.moodLine)
    }

    @Test
    fun `onTempUnitToggled writes fahrenheit when current is celsius`() =
        runTest(testDispatcher) {
            setupPrefs(tempUnit = "celsius")

            val viewModel = SettingsViewModel(mockDataStore, mockBillingRepository)
            viewModel.onTempUnitToggled()
            advanceUntilIdle()

            verify {
                mockMutablePrefs.set(PreferenceKeys.KEY_TEMP_UNIT, "fahrenheit")
            }
        }

    @Test
    fun `onTempUnitToggled writes celsius when current is fahrenheit`() =
        runTest(testDispatcher) {
            setupPrefs(tempUnit = "fahrenheit")
            val secondPrefs = mockk<Preferences>(relaxed = true)
            every { secondPrefs[PreferenceKeys.KEY_TEMP_UNIT] } returns "fahrenheit"
            every { secondPrefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns false
            every { secondPrefs[PreferenceKeys.KEY_IS_PREMIUM] } returns false
            every { secondPrefs[PreferenceKeys.KEY_MOOD_LINE] } returns ""
            every { secondPrefs[PreferenceKeys.KEY_PERSONALITY_CORE] } returns null
            every { mockDataStore.data } returns flowOf(secondPrefs)

            val viewModel = SettingsViewModel(mockDataStore, mockBillingRepository)
            viewModel.onTempUnitToggled()
            advanceUntilIdle()

            verify {
                mockMutablePrefs.set(PreferenceKeys.KEY_TEMP_UNIT, "celsius")
            }
        }

    @Test
    fun `shareText format is correct`() = runTest(testDispatcher) {
        val mood = "Honestly lovely today. Eat lunch outside."
        setupPrefs(moodLine = mood)

        val viewModel = SettingsViewModel(mockDataStore, mockBillingRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Success", state is UiState.Success)
        val data = (state as UiState.Success).data
        val expectedShare = "\"$mood\"\n\nWeatherApp — daily weather in plain language"
        assertEquals(expectedShare, data.shareText)
    }

    @Test
    fun `onNotificationsToggled flips notifications enabled`() =
        runTest(testDispatcher) {
            setupPrefs(notificationsEnabled = false)

            val viewModel = SettingsViewModel(mockDataStore, mockBillingRepository)
            viewModel.onNotificationsToggled()
            advanceUntilIdle()

            verify {
                mockMutablePrefs.set(PreferenceKeys.KEY_NOTIFICATIONS_ENABLED, true)
            }
        }
}

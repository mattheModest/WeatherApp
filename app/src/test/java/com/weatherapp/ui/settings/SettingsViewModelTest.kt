package com.weatherapp.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.util.UiState
import io.mockk.coAnswers
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var mockPrefs: Preferences
    private lateinit var mockMutablePrefs: MutablePreferences

    @Before
    fun setUp() {
        mockPrefs = mockk<Preferences>(relaxed = true)
        mockMutablePrefs = mockk(relaxed = true)

        mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        every { mockDataStore.data } returns flowOf(mockPrefs)
        coEvery { mockDataStore.edit(any()) } coAnswers {
            firstArg<suspend (MutablePreferences) -> Unit>().invoke(mockMutablePrefs)
            mockPrefs
        }
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
    }

    @Test
    fun `loads settings from DataStore correctly`() = runTest(UnconfinedTestDispatcher()) {
        setupPrefs(
            tempUnit = "celsius",
            notificationsEnabled = true,
            isPremium = false,
            moodLine = "Grey but manageable."
        )

        val viewModel = SettingsViewModel(mockDataStore)

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
        runTest(UnconfinedTestDispatcher()) {
            setupPrefs(tempUnit = "celsius")

            val viewModel = SettingsViewModel(mockDataStore)
            viewModel.onTempUnitToggled()

            verify {
                mockMutablePrefs.set(PreferenceKeys.KEY_TEMP_UNIT, "fahrenheit")
            }
        }

    @Test
    fun `onTempUnitToggled writes celsius when current is fahrenheit`() =
        runTest(UnconfinedTestDispatcher()) {
            setupPrefs(tempUnit = "fahrenheit")
            // Return fahrenheit for the first() call inside onTempUnitToggled
            val secondPrefs = mockk<Preferences>(relaxed = true)
            every { secondPrefs[PreferenceKeys.KEY_TEMP_UNIT] } returns "fahrenheit"
            every { secondPrefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns false
            every { secondPrefs[PreferenceKeys.KEY_IS_PREMIUM] } returns false
            every { secondPrefs[PreferenceKeys.KEY_MOOD_LINE] } returns ""
            every { mockDataStore.data } returns flowOf(secondPrefs)

            val viewModel = SettingsViewModel(mockDataStore)
            viewModel.onTempUnitToggled()

            verify {
                mockMutablePrefs.set(PreferenceKeys.KEY_TEMP_UNIT, "celsius")
            }
        }

    @Test
    fun `shareText format is correct`() = runTest(UnconfinedTestDispatcher()) {
        val mood = "Honestly lovely today. Eat lunch outside."
        setupPrefs(moodLine = mood)

        val viewModel = SettingsViewModel(mockDataStore)

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Success", state is UiState.Success)
        val data = (state as UiState.Success).data
        val expectedShare = "\"$mood\"\n\nWeatherApp — daily weather in plain language"
        assertEquals(expectedShare, data.shareText)
    }

    @Test
    fun `onNotificationsToggled flips notifications enabled`() =
        runTest(UnconfinedTestDispatcher()) {
            setupPrefs(notificationsEnabled = false)

            val viewModel = SettingsViewModel(mockDataStore)
            viewModel.onNotificationsToggled()

            verify {
                mockMutablePrefs.set(PreferenceKeys.KEY_NOTIFICATIONS_ENABLED, true)
            }
        }
}

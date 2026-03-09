package com.weatherapp.ui.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.datastore.preferences.core.edit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.weatherapp.util.UiState

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val editSlot = slot<suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit>()

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var mockWorkManager: WorkManager
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: Preferences
    private lateinit var mockMutablePrefs: androidx.datastore.preferences.core.MutablePreferences

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        mockPrefs = mockk<Preferences>(relaxed = true)
        every { mockPrefs[any<Preferences.Key<Boolean>>()] } returns null
        every { mockPrefs[any<Preferences.Key<String>>()] } returns null

        mockMutablePrefs = mockk(relaxed = true)

        mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        every { mockDataStore.data } returns flowOf(mockPrefs)
        coEvery { mockDataStore.edit(capture(editSlot)) } coAnswers {
            editSlot.captured.invoke(mockMutablePrefs)
            mockPrefs
        }

        mockWorkManager = mockk<WorkManager>(relaxed = true)
        every { mockWorkManager.enqueue(any<WorkRequest>()) } returns mockk(relaxed = true)
        every { mockWorkManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk(relaxed = true)

        mockContext = mockk<Context>(relaxed = true)
        every { mockContext.applicationContext } returns mockContext

        viewModel = OnboardingViewModel(
            dataStore = mockDataStore,
            workManager = mockWorkManager,
            context = mockContext
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state is Success with LOCATION_PERMISSION step`() = runTest(testDispatcher) {
        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Success", state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(OnboardingStep.LOCATION_PERMISSION, data.step)
        assertEquals(false, data.locationDenied)
        assertEquals(false, data.calendarDenied)
    }

    @Test
    fun `onLocationGranted advances step to CALENDAR_RATIONALE`() = runTest(testDispatcher) {
        viewModel.onLocationGranted()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Success", state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(OnboardingStep.CALENDAR_RATIONALE, data.step)
        assertEquals(false, data.locationDenied)
    }

    @Test
    fun `onLocationDenied advances step to MANUAL_LOCATION and sets locationDenied true`() =
        runTest(testDispatcher) {
            viewModel.onLocationDenied()

            val state = viewModel.uiState.value
            assertTrue("Expected UiState.Success", state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(OnboardingStep.MANUAL_LOCATION, data.step)
            assertEquals(true, data.locationDenied)
        }

    @Test
    fun `onCalendarDenied writes KEY_HAS_COMPLETED_ONBOARDING true to DataStore`() =
        runTest(testDispatcher) {
            viewModel.onLocationGranted()
            viewModel.onCalendarRationaleAcknowledged()
            viewModel.onCalendarDenied()
            advanceUntilIdle()

            coVerify {
                mockDataStore.edit(any())
            }
            coVerify {
                mockMutablePrefs.set(
                    com.weatherapp.data.datastore.PreferenceKeys.KEY_HAS_COMPLETED_ONBOARDING,
                    true
                )
            }
        }

    @Test
    fun `onCalendarGranted writes KEY_HAS_COMPLETED_ONBOARDING true to DataStore`() =
        runTest(testDispatcher) {
            viewModel.onLocationGranted()
            viewModel.onCalendarRationaleAcknowledged()
            viewModel.onCalendarGranted()
            advanceUntilIdle()

            coVerify {
                mockDataStore.edit(any())
            }
            coVerify {
                mockMutablePrefs.set(
                    com.weatherapp.data.datastore.PreferenceKeys.KEY_HAS_COMPLETED_ONBOARDING,
                    true
                )
            }
        }

    @Test
    fun `onCalendarGranted enqueues work via WorkManager`() =
        runTest(testDispatcher) {
            viewModel.onLocationGranted()
            viewModel.onCalendarRationaleAcknowledged()
            viewModel.onCalendarGranted()
            advanceUntilIdle()

            coVerify { mockWorkManager.enqueue(any<WorkRequest>()) }
            coVerify { mockWorkManager.enqueueUniquePeriodicWork(any(), any(), any()) }
        }
}

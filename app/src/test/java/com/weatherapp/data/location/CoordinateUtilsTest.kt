package com.weatherapp.data.location

import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateUtilsTest {

    private val delta = 0.0001

    @Test
    fun `snapToGrid rounds positive value down to nearest grid point`() {
        // 37.83 is closer to 37.8
        assertEquals(37.8, 37.83.snapToGrid(), delta)
    }

    @Test
    fun `snapToGrid rounds positive value up to nearest grid point`() {
        // 37.87 is closer to 37.9
        assertEquals(37.9, 37.87.snapToGrid(), delta)
    }

    @Test
    fun `snapToGrid rounds negative value toward nearest grid point`() {
        // -122.43 is closer to -122.4 (nearer to zero)
        assertEquals(-122.4, (-122.43).snapToGrid(), delta)
        // -122.47 is closer to -122.5
        assertEquals(-122.5, (-122.47).snapToGrid(), delta)
    }

    @Test
    fun `snapToGrid returns zero for zero input`() {
        assertEquals(0.0, 0.0.snapToGrid(), delta)
    }

    @Test
    fun `snapToGrid handles value at exact grid boundary`() {
        assertEquals(37.8, 37.8.snapToGrid(), delta)
        assertEquals(-122.4, (-122.4).snapToGrid(), delta)
    }

    @Test
    fun `snapToGrid handles large coordinate values`() {
        assertEquals(90.0, 90.0.snapToGrid(), delta)
        assertEquals(-180.0, (-180.0).snapToGrid(), delta)
    }

    @Test
    fun `snapToGrid respects custom cellDegrees`() {
        assertEquals(38.0, 37.83.snapToGrid(1.0), delta)
        assertEquals(37.5, 37.6.snapToGrid(0.5), delta)
    }
}

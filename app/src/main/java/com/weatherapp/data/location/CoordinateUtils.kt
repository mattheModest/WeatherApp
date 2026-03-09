package com.weatherapp.data.location

import kotlin.math.roundToInt

fun Double.snapToGrid(cellDegrees: Double = 0.1): Double =
    (this / cellDegrees).roundToInt() * cellDegrees

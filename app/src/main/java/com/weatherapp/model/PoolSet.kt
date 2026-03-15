package com.weatherapp.model

internal typealias ZonedPool = Map<ClimateZone, List<String>>

internal interface PoolSet {
    val stormVerdict: ZonedPool
    val heavyRainVerdict: ZonedPool
    val rainVerdict: ZonedPool
    val drizzleVerdict: ZonedPool
    val snowVerdict: ZonedPool
    val veryWindyVerdict: ZonedPool
    val windyVerdict: ZonedPool
    val hotVerdict: ZonedPool
    val warmVerdict: ZonedPool
    val lightJacketVerdict: ZonedPool
    val jacketVerdict: ZonedPool
    val bundleUpVerdict: ZonedPool
    val allClearVerdict: ZonedPool
    val stormMood: ZonedPool
    val heavyRainMood: ZonedPool
    val rainMood: ZonedPool
    val drizzleMood: ZonedPool
    val snowMood: ZonedPool
    val windMood: ZonedPool
    val breezeMood: ZonedPool
    val allClearWarmMood: ZonedPool
    val allClearNeutralMood: ZonedPool
    val greyMood: ZonedPool
}

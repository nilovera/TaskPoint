package com.example.apk_mock.data.geocoding

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Great-circle distance in metres; it works offline with persisted coordinates. */
internal fun distanceMeters(
    originLatitude: Double,
    originLongitude: Double,
    destinationLatitude: Double,
    destinationLongitude: Double
): Int {
    val latitudeDelta = Math.toRadians(destinationLatitude - originLatitude)
    val longitudeDelta = Math.toRadians(destinationLongitude - originLongitude)
    val originLatitudeRadians = Math.toRadians(originLatitude)
    val destinationLatitudeRadians = Math.toRadians(destinationLatitude)
    val haversine = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
        cos(originLatitudeRadians) * cos(destinationLatitudeRadians) *
        sin(longitudeDelta / 2) * sin(longitudeDelta / 2)

    return (EARTH_RADIUS_METERS * 2 * asin(sqrt(haversine.coerceIn(0.0, 1.0)))).roundToInt()
}

private const val EARTH_RADIUS_METERS = 6_371_000.0

package com.example.apk_mock.data.geocoding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoDistanceTest {

    @Test
    fun sameCoordinatesHaveZeroDistance() {
        assertEquals(0, distanceMeters(-34.6037, -58.3816, -34.6037, -58.3816))
    }

    @Test
    fun oneDegreeOfLongitudeAtEquatorIsAboutOneHundredElevenKilometres() {
        val metres = distanceMeters(0.0, 0.0, 0.0, 1.0)

        assertTrue(metres in 111_000..112_000)
    }
}

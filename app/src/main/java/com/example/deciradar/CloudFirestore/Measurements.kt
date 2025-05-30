package com.example.deciradar.CloudFirestore

import com.google.firebase.firestore.PropertyName

/**
 * Reprezentuje pojedynczy pomiar poziomu dźwięku wykonany przez użytkownika.
 */
data class Measurements(
    @get:PropertyName("userID") @set:PropertyName("userID")
    var userID: String = "",

    @get:PropertyName("date") @set:PropertyName("date")
    var date: String = "",

    @get:PropertyName("hour") @set:PropertyName("hour")
    var hour: String = "",

    @get:PropertyName("soundIntensity") @set:PropertyName("soundIntensity")
    var soundIntensity: String = "",

    @get:PropertyName("lat") @set:PropertyName("lat")
    var lat: Double? = null,

    @get:PropertyName("lng") @set:PropertyName("lng")
    var lng: Double? = null
)

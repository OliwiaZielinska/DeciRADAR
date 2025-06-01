package com.example.deciradar.CloudFirestore

import com.google.firebase.firestore.PropertyName

/**
 * Reprezentuje pojedynczy pomiar poziomu dźwięku wykonany przez użytkownika, przechowywany w bazie Firestore.
 *
 * @property userID Unikalny identyfikator użytkownika, który wykonał pomiar.
 * @property date Data wykonania pomiaru w formacie "yyyy-MM-dd".
 * @property hour Godzina wykonania pomiaru w formacie "HH:mm".
 * @property soundIntensity Natężenie dźwięku jako tekst, zwykle w decybelach (dB).
 * @property lat Szerokość geograficzna miejsca, w którym wykonano pomiar.
 * @property lng Długość geograficzna miejsca, w którym wykonano pomiar.
 */
data class Measurements(

    /** Unikalny identyfikator użytkownika. */
    @get:PropertyName("userID") @set:PropertyName("userID")
    var userID: String = "",

    /** Data wykonania pomiaru w formacie "yyyy-MM-dd". */
    @get:PropertyName("date") @set:PropertyName("date")
    var date: String = "",

    /** Godzina wykonania pomiaru w formacie "HH:mm". */
    @get:PropertyName("hour") @set:PropertyName("hour")
    var hour: String = "",

    /** Natężenie dźwięku w formie tekstowej, np. "72.5". */
    @get:PropertyName("soundIntensity") @set:PropertyName("soundIntensity")
    var soundIntensity: String = "",

    /** Szerokość geograficzna miejsca pomiaru. */
    @get:PropertyName("lat") @set:PropertyName("lat")
    var lat: Double? = null,

    /** Długość geograficzna miejsca pomiaru. */
    @get:PropertyName("lng") @set:PropertyName("lng")
    var lng: Double? = null
)

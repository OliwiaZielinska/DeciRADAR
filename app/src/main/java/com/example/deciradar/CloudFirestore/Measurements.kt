package com.example.deciradar.CloudFirestore

import com.google.firebase.firestore.PropertyName
/**
 * Reprezentuje pojedynczy pomiar poziomu dźwięku wykonany przez użytkownika.
 *
 * Klasa wykorzystywana do przechowywania danych w Firestore.
 * Każde pole jest powiązane z odpowiednią nazwą właściwości w dokumentach Firestore za pomocą adnotacji [PropertyName].
 *
 * @property userID Identyfikator użytkownika, który wykonał pomiar.
 * @property date Data wykonania pomiaru (np. "2025-04-25").
 * @property hour Godzina wykonania pomiaru (np. "14:30").
 * @property soundIntensity Wartość zmierzonego poziomu natężenia dźwięku (np. w dB).
 */
data class Measurements(
    @get:PropertyName("userID") @set:PropertyName("userID") var userID: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("hour") @set:PropertyName("hour") var hour: String = "",
    @get:PropertyName("soundIntensity") @set:PropertyName("soundIntensity") var soundIntensity: String = "",
)
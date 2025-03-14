package com.example.deciradar

/**
 * Reprezentuje dane dotyczące pomiaru dźwięku.
 * @property date Data wykonania pomiaru w formacie "yyyy-MM-dd".
 * @property hour Godzina wykonania pomiaru w formacie "HH:mm".
 * @property soundIntensity Natężenie dźwięku zapisane jako String.
 * @property userID Identyfikator użytkownika, do którego należą dane.
 */
data class SoundData(
    val date: String = "",
    val hour: String = "",
    val soundIntensity: String = "",
    val userID: String = ""
)
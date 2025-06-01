package com.example.deciradar

/**
 * Reprezentuje dane dotyczące pojedynczego pomiaru dźwięku, wykorzystywane m.in. przy wyświetlaniu
 * historii pomiarów użytkownika oraz zapisie do bazy danych.
 *
 * @property date Data wykonania pomiaru w formacie "yyyy-MM-dd".
 * @property hour Godzina wykonania pomiaru w formacie "HH:mm".
 * @property soundIntensity Natężenie dźwięku w decybelach, zapisane jako tekst.
 * @property userID Unikalny identyfikator użytkownika, który wykonał pomiar.
 * @property lat Opcjonalna szerokość geograficzna miejsca pomiaru (jeśli dostępna).
 * @property lng Opcjonalna długość geograficzna miejsca pomiaru (jeśli dostępna).
 * @property documentId Opcjonalny identyfikator dokumentu w Firestore (przydatny do usuwania).
 */
data class SoundData(
    val date: String = "",
    val hour: String = "",
    val soundIntensity: String = "",
    val userID: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val documentId: String? = null
)

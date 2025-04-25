package com.example.deciradar.CloudFirestore

import com.google.firebase.firestore.PropertyName
/**
 * Reprezentuje dane użytkownika aplikacji, przechowywane w bazie Firestore.
 *
 * Każde pole jest mapowane na odpowiadającą mu nazwę w dokumentach Firestore
 * przy pomocy adnotacji [PropertyName], co pozwala na zachowanie zgodności z nazwami w bazie.
 *
 * @property name Imię użytkownika.
 * @property surname Nazwisko użytkownika.
 * @property login Nazwa użytkownika używana do logowania.
 * @property password Hasło użytkownika (uwaga: przechowywanie haseł w postaci jawnej nie jest bezpieczne).
 * @property endNightMode Godzina zakończenia trybu nocnego (np. "07:00").
 * @property startNightMode Godzina rozpoczęcia trybu nocnego (np. "22:00").
 */
data class User(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("surname") @set:PropertyName("surname") var surname: String = "",
    @get:PropertyName("login") @set:PropertyName("login") var login: String = "",
    @get:PropertyName("password") @set:PropertyName("password") var password: String = "",
    @get:PropertyName("endNightMode") @set:PropertyName("endNightMode") var endNightMode: String = "",
    @get:PropertyName("startNightMode") @set:PropertyName("startNightMode") var startNightMode: String = "",
)
package com.example.deciradar.CloudFirestore
/**
 * Interfejs definiujący operacje wykonywane na bazie danych Firestore
 * związane z użytkownikami i ich pomiarami.
 */
interface FirestoreInterface {
    /**
     * Dodaje nowego użytkownika do bazy danych.
     *
     * @param userId Unikalny identyfikator użytkownika (dokumentu).
     * @param user Obiekt [User] zawierający dane użytkownika do zapisania.
     */
    suspend fun addUser(userId: String, user: User)
    /**
     * Pobiera dane użytkownika na podstawie jego identyfikatora.
     *
     * @param userId Identyfikator użytkownika do pobrania.
     * @return Obiekt [User], jeśli użytkownik istnieje, w przeciwnym razie `null`.
     */
    suspend fun getUser(userId: String): User?
    /**
     * Aktualizuje dane użytkownika w bazie.
     *
     * @param userId Identyfikator użytkownika do zaktualizowania.
     * @param updatedUser Obiekt [User] zawierający zaktualizowane dane.
     */
    suspend fun updateUser(userId: String, updatedUser: User)
    /**
     * Usuwa użytkownika oraz jego dane z bazy.
     *
     * @param userId Identyfikator użytkownika do usunięcia.
     */
    suspend fun deleteUser(userId: String)
    /**
     * Dodaje nowy pomiar do bazy danych dla konkretnego użytkownika.
     *
     * @param userId Identyfikator użytkownika, do którego należy pomiar.
     * @param measurmentID Unikalny identyfikator pomiaru.
     * @param measurment Obiekt [Measurements] zawierający dane pomiaru.
     */
    suspend fun addMeasurement(userId: String, measurmentID: String, measurment: Measurements)
}
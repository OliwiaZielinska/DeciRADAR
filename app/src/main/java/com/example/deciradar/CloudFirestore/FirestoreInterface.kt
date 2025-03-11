package com.example.deciradar.CloudFirestore

interface FirestoreInterface {
    suspend fun addUser(userId: String, user: User)

    suspend fun getUser(userId: String): User?

    suspend fun updateUser(userId: String, updatedUser: User)

    suspend fun deleteUser(userId: String)

    suspend fun addMeasurement(userId: String, measurmentID: String, measurment: Measurements)
}
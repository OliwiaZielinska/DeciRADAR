package com.example.deciradar.CloudFirestore

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreDatabaseOperations(private val db: FirebaseFirestore) : FirestoreInterface {
    override suspend fun addUser(userId: String, user: User) {
        try {
            db.collection("users").document(userId).set(user).await()
        }catch (e: Exception) {
            Log.e("addUser", "Błąd podczas dodawania użytkownika: $e")
        }
    }

    override suspend fun getUser(userId: String): User? {
        val snapshot = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo(FieldPath.documentId(), userId)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }

    override suspend fun updateUser(userId: String, updatedUser: User) {
        try {
            db.collection("users").document(userId).set(updatedUser).await()
        } catch (e: Exception) {
            Log.e("updateUser", "Błąd podczas aktualizacji danych użytkownika: $e")
        }
    }

    override suspend fun deleteUser(userId: String) {
        try {
            val collectionRef = FirebaseFirestore.getInstance().collection("measurements")
            collectionRef.whereEqualTo("userID", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot usunięto poprawnie!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Błąd podczas usuwania dokumentu", e)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Błąd podczas pobierania dokumentu: ", exception)
                }
            db.collection("users").document(userId).delete().await()
            val user = FirebaseAuth.getInstance().currentUser
            user?.delete()
        } catch (e: Exception) {
            Log.e("deleteUser", "Błąd podczas usuwania użytkownika: $e")
        }
    }

    override suspend fun addMeasurement(userId: String, measurmentID:String,  measurment: Measurements) {
        try {
            db.collection("measurments").document(userId).collection("measurment").document(measurmentID).set(measurment).await()
        }catch (e: Exception) {
            Log.e("addMeasurment", "Błąd podczas dodawania pomiaru: $e")
        }
    }

}
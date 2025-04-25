package com.example.deciradar.CloudFirestore

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
/**
 * Implementacja interfejsu [FirestoreInterface], która umożliwia operacje CRUD
 * na użytkownikach i pomiarach z użyciem Firebase Firestore.
 *
 * @property db Instancja [FirebaseFirestore] używana do wykonywania operacji.
 */
class FirestoreDatabaseOperations(private val db: FirebaseFirestore) : FirestoreInterface {

    /**
     * Dodaje nowego użytkownika do kolekcji "users" w Firestore.
     *
     * @param userId Unikalny identyfikator użytkownika (dokumentu).
     * @param user Obiekt [User] zawierający dane użytkownika do zapisania.
     */
    override suspend fun addUser(userId: String, user: User) {
        try {
            db.collection("users").document(userId).set(user).await()
        }catch (e: Exception) {
            Log.e("addUser", "Błąd podczas dodawania użytkownika: $e")
        }
    }
    /**
     * Pobiera dane użytkownika z Firestore na podstawie jego ID.
     *
     * @param userId Identyfikator użytkownika (dokumentu).
     * @return Obiekt [User], jeśli istnieje, w przeciwnym razie `null`.
     */
    override suspend fun getUser(userId: String): User? {
        val snapshot = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo(FieldPath.documentId(), userId)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }
    /**
     * Aktualizuje dane użytkownika w kolekcji "users".
     *
     * @param userId Identyfikator użytkownika do zaktualizowania.
     * @param updatedUser Nowe dane użytkownika w postaci obiektu [User].
     */
    override suspend fun updateUser(userId: String, updatedUser: User) {
        try {
            db.collection("users").document(userId).set(updatedUser).await()
        } catch (e: Exception) {
            Log.e("updateUser", "Błąd podczas aktualizacji danych użytkownika: $e")
        }
    }
    /**
     * Usuwa użytkownika z Firestore oraz powiązane z nim pomiary z kolekcji "measurements".
     * Dodatkowo usuwa konto użytkownika z Firebase Authentication, jeśli jest zalogowany.
     *
     * @param userId Identyfikator użytkownika do usunięcia.
     */
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
    /**
     * Dodaje nowy pomiar do podkolekcji "measurment" w dokumencie użytkownika.
     *
     * @param userId Identyfikator użytkownika, do którego przypisany jest pomiar.
     * @param measurmentID Unikalny identyfikator pomiaru.
     * @param measurment Obiekt [Measurements] zawierający dane pomiaru.
     */
    override suspend fun addMeasurement(userId: String, measurmentID:String,  measurment: Measurements) {
        try {
            db.collection("measurments").document(userId).collection("measurment").document(measurmentID).set(measurment).await()
        }catch (e: Exception) {
            Log.e("addMeasurment", "Błąd podczas dodawania pomiaru: $e")
        }
    }

}
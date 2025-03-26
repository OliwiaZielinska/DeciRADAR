package com.example.deciradar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Serwis obsługujący wiadomości Firebase Cloud Messaging (FCM), odpowiada za odbieranie i obsługę
 * wiadomości push oraz generowanie powiadomień.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Funkcja wywoływana, gdy Firebase odświeży token urządzenia.
     * @param token Nowy token FCM urządzenia.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nowy token: $token")
    }

    /**
     * Funkcja wywoływana po otrzymaniu nowej wiadomości FCM.
     * @param remoteMessage Otrzymana wiadomość z FCM.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            sendNotification(it.body ?: "Nowa wiadomość!")
        }
    }

    /**
     * Funkcja umożliwiająca tworzenie i wyświetlanie powiadomień na podstawie treści wiadomości.
     * @param messageBody Treść wiadomości do wyświetlenia w powiadomieniu.
     */
    private fun sendNotification(messageBody: String) {
        val channelId = "push_channel"
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.radar)
            .setContentTitle("Powiadomienie")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Powiadomienia", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
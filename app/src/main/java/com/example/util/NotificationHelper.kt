package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R

object NotificationHelper {
  private const val CHANNEL_ID = "github_app_updates"
  private const val CHANNEL_NAME = "Pembaruan Aplikasi GitHub"
  private const val NOTIFICATION_ID = 2001

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notifikasi rilis versi terbaru atau pembaruan kode dari GitHub"
        enableLights(true)
        enableVibration(true)
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      manager?.createNotificationChannel(channel)
    }
  }

  fun showUpdateNotification(
    context: Context,
    title: String,
    message: String,
    downloadUrl: String
  ) {
    createNotificationChannel(context)

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      NOTIFICATION_ID,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .addAction(
        android.R.drawable.stat_sys_download,
        "Unduh APK",
        pendingIntent
      )

    try {
      val manager = NotificationManagerCompat.from(context)
      manager.notify(NOTIFICATION_ID, builder.build())
    } catch (_: SecurityException) {
      // Permission might not be granted yet on Android 13+
    }
  }
}

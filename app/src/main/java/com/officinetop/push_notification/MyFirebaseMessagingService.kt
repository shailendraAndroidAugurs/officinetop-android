package com.officinetop.push_notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.officinetop.R
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseToken"
    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "officinetop"

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i(TAG, p0)
        Log.e("messageonNewtokens==", p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.e("messages==", p0.toString())
        Log.e("messages1==", p0.notification?.body)
        Log.e("messages2==", "" + p0.data.size)
        if (p0.notification != null) {
            Log.i(TAG, p0.notification?.title!!)
            Log.i(TAG, p0.notification?.body!!)

        }
        p0.let { message ->

            // Create an Intent for the activity you want to start
            val resultIntent = Intent(this, NotificationList::class.java)
            // Create the TaskStackBuilder
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
                // Add the intent, which inflates the back stack
                addNextIntentWithParentStack(resultIntent)
                // Get the PendingIntent containing the entire back stack
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }


            // Log.i(TAG, message.getData().get("message"))
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //Setting up Notification channels for android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupNotificationChannels()
            }

            val notificationId = Random().nextInt(60000)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID).apply {
                setContentIntent(resultPendingIntent)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setSmallIcon(R.mipmap.ic_stat_app_icon)  //a resource for your custom small
                notificationBuilder.color = resources.getColor(R.color.color9)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    notificationBuilder.setContentTitle(Html.fromHtml(p0.notification?.title.toString(), Html.FROM_HTML_MODE_COMPACT))
                    notificationBuilder.setContentText(Html.fromHtml(p0.notification?.body.toString(), Html.FROM_HTML_MODE_COMPACT))
                } else {
                    notificationBuilder.setContentTitle(Html.fromHtml(p0.notification?.title))
                    notificationBuilder.setContentText(Html.fromHtml(p0.notification?.body))

                }
                notificationBuilder.setAutoCancel(true)  //dismisses the notification on click
                notificationBuilder.setColorized(true)
                notificationBuilder.setSound(defaultSoundUri)
            } else {
                notificationBuilder.setSmallIcon(R.mipmap.ic_stat_app_icon)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    notificationBuilder.setContentTitle(Html.fromHtml(p0.notification?.title.toString(), Html.FROM_HTML_MODE_COMPACT))
                    notificationBuilder.setContentText(Html.fromHtml(p0.notification?.body.toString(), Html.FROM_HTML_MODE_COMPACT))


                } else {
                    notificationBuilder.setContentTitle(Html.fromHtml(p0.notification?.title.toString()))
                    notificationBuilder.setContentText(Html.fromHtml(p0.notification?.body.toString()))

                }

                notificationBuilder.setAutoCancel(true)
                notificationBuilder.setColorized(true)
                notificationBuilder.setSound(defaultSoundUri)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build())

            val notifyIntent = Intent(this, NotificationList::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }


        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels() {
        val adminChannelName = getString(R.string.notifications_admin_channel_name)
        val adminChannelDescription = getString(R.string.notifications_admin_channel_description)
        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager.createNotificationChannel(adminChannel)
    }
}
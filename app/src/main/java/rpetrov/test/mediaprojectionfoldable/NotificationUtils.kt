package rpetrov.test.mediaprojectionfoldable

import android.app.NotificationManager
import android.annotation.TargetApi
import android.app.Notification
import android.os.Build
import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationUtils {
    const val NOTIFICATION_ID = 1337
    private const val NOTIFICATION_CHANNEL_ID = "mediaprojectionleakdemo"
    private const val NOTIFICATION_CHANNEL_NAME = "mediaprojectionleakdemo"
    fun getNotification(context: Context): Pair<Int, Notification> {
        createNotificationChannel(context)
        val notification = createNotification(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        return Pair(NOTIFICATION_ID, notification)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(context: Context): Notification {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        builder.setContentTitle(context.getString(R.string.app_name))
        builder.setContentText("MediaProjectionLeakDemo")
        builder.setOngoing(true)
        builder.setCategory(Notification.CATEGORY_SERVICE)
        builder.priority = Notification.PRIORITY_LOW
        builder.setShowWhen(true)
        return builder.build()
    }
}
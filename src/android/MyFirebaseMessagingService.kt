package notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService: FirebaseMessagingService() {
  companion object {
    private const val TAG = "pushNotification"
    private const val CHANNEL_NAME = "Default channel"
  }
  private val mainfestIconKey = "com.google.firebase.messaging.default_notification_icon"
  private val mainfestChannelKey = "com.google.firebase.messaging.default_notification_channel_id"
  private val mainfestColorKey = "com.google.firebase.messaging.default_notification_color"

  private var defaultNotificationIcon = 0
  private var defaultNotificationColor = 0
  private var defaultNotificationChannelID = ""
  private var notificationManager: NotificationManager? = null

  private var mainActivity: Class<*>? = null

  override fun onCreate() {
    notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)

    try {
      val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(applicationContext.packageName)
      val className = launchIntent?.component?.className as String
      mainActivity = Class.forName(className)

      val ai: ApplicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getApplicationInfo(
          applicationContext.packageName,
          PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        )
      } else {
        packageManager.getApplicationInfo(
          applicationContext.packageName,
          PackageManager.GET_META_DATA
        )
      }

      defaultNotificationChannelID = ai.metaData.getString(mainfestChannelKey, "444")
      val channel: NotificationChannel
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        channel = NotificationChannel(
          defaultNotificationChannelID,
          CHANNEL_NAME,
          NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager!!.createNotificationChannel(channel)
      }

      defaultNotificationIcon = ai.metaData.getInt(mainfestIconKey, ai.icon)
      defaultNotificationColor= ai.metaData.getInt(mainfestColorKey, 0)
    } catch (e: PackageManager.NameNotFoundException) {
      Log.e(TAG, "Failed to load data from AndroidManifest.xml", e)
    }
    super.onCreate()
  }

  override fun onMessageReceived(p0: RemoteMessage) {
    super.onMessageReceived(p0)
    sendNotification(p0)
  }

  private fun sendNotification(p0: RemoteMessage) {
    val notification = p0.notification
    val data = p0.data
    val title = notification?.title
    val body = notification?.body

    if (title == null || body == null) { return }

    var notificationId: Int? = null

    val payload = data["payload"]
    var channelId = data["channelId"]
    val notificationIdFromData = data["notificationId"]
    if (notificationIdFromData != null) {
      notificationId = notificationIdFromData.toInt()
    }

    val notifyIntent = Intent(this, mainActivity).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

      if (payload != null) {
        putExtra(Intent.EXTRA_TEXT, payload)
      }
    }

    if (channelId == null) {
      channelId = defaultNotificationChannelID
    }

    if (notificationId == null) {
      notificationId = (1..2147483647).random()
    }

    val notifyPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
      addNextIntentWithParentStack(notifyIntent)

      val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
         PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
      } else {
        PendingIntent.FLAG_CANCEL_CURRENT
      }

      getPendingIntent(0, pendingFlags)
    }

    val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/raw/" + channelId)

    val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
      setSmallIcon(defaultNotificationIcon)
      setColor(defaultNotificationColor)
      setSound(soundUri)
      setContentTitle(title)
      setContentText(body)
      setPriority(NotificationCompat.PRIORITY_HIGH)
      setAutoCancel(true)
      setContentIntent(notifyPendingIntent)
    }

    val context = this
    with(NotificationManagerCompat.from(this)) {
      if (ActivityCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) { return }
      notify(notificationId, notificationBuilder.build())
    }
  }
}

package notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri;
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger


class MyFirebaseMessagingService: FirebaseMessagingService() {
  companion object {
    private const val TAG = "pushNotification"
  }
  val mainfestIconKey     = "com.google.firebase.messaging.default_notification_icon"
  val mainfestChannelKey  = "com.google.firebase.messaging.default_notification_channel_id"
  val mainfestColorKey    = "com.google.firebase.messaging.default_notification_color"

  private var defaultNotificationIcon = 0
  private var defaultNotificationColor = 0
  private var defaultNotificationChannelID = ""
  private var notificationManager: NotificationManager? = null

  var mainActivity: Class<*>? = null

  override fun onCreate() {
    notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)

    try {
      // Get MainActivity without import
      var launchIntent: Intent? = packageManager.getLaunchIntentForPackage(applicationContext.packageName)
      var className = launchIntent?.component?.className as String
      mainActivity = Class.forName(className)

      // Other
      val ai = packageManager.getApplicationInfo(
        applicationContext.packageName,
        PackageManager.GET_META_DATA
      )

      defaultNotificationChannelID = ai.metaData.getString(mainfestChannelKey, "444")
      val channel: NotificationChannel
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        channel = NotificationChannel(
          defaultNotificationChannelID,
          "PUSH NOTIFICATIONS",
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

    if (p0 !== null) {
      sendNotification(p0)
    }
  }

  private fun sendNotification(p0: RemoteMessage){
    val data      =p0.data
    val title     =data["title"]
    val body      =data["body"]
    val payload   =data["payload"]
    var channel_id=data["channel_id"]
    //var channel_id = data["android"]["notification"]["channel_id"] //<--@TODO: make it compliant with official notification standard

    var resultIntent = Intent(this, mainActivity)
    if (payload != null) {
      // For launch
      resultIntent.putExtra("pushNotification", payload)
    }

    if(channel_id==null){
      channel_id=defaultNotificationChannelID
    }

    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
      addNextIntentWithParentStack(resultIntent)
      getPendingIntent(101, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    // Create notification
    var soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+applicationContext.packageName+"/raw/"+channel_id)

    val notificationBuilder = NotificationCompat.Builder(this, channel_id)
      .setSmallIcon(defaultNotificationIcon)
      .setColor(defaultNotificationColor)
      .setSound(soundUri)
      .setContentTitle(title)
      .setContentText(body)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(resultPendingIntent)

    with(NotificationManagerCompat.from(this)) {
      val notificationId = (AtomicInteger(0)).incrementAndGet()
      notify(notificationId, notificationBuilder.build())
    }
  }
}

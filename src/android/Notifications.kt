package notifications

import android.content.Intent
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import org.json.JSONException


class Notifications: CordovaPlugin() {
  companion object {
    private const val TAG = "pushNotification"
  }

  @Throws(JSONException::class)
  override fun execute(action: String, data: JSONArray, callbackContext: CallbackContext): Boolean {
    var result = true
    try {
      when (action) {
        "registration" -> {
          Log.d(TAG, "registration")

          cordova.threadPool.execute {
            getFirebaseToken(callbackContext)
          }
        }
        "tapped" -> {
          Log.d(TAG, "tapped")

          val activity = cordova.activity
          val extraText = activity.intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

          cordova.threadPool.execute {
            callbackContext.success(extraText)
          }
        }
        else -> {
          handleError("Invalid action", callbackContext)
          result = false
        }
      }
    } catch (e: Exception) {
      handleError(e.toString(), callbackContext)
      result = false
    }

    return result
  }

  private fun getFirebaseToken(context: CallbackContext) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
      if (!task.isSuccessful) {
        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
        return@OnCompleteListener
      }

      val token = task.result
      Log.d(TAG, token)
      context.success(token)
    })
  }

  private fun handleError(errorMsg: String, context: CallbackContext) {
    try {
      Log.e(TAG, errorMsg)
      context.error(errorMsg)
    } catch (e: Exception) {
      Log.e(TAG, e.toString())
    }
  }
}

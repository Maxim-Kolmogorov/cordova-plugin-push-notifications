package notifications

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import org.json.JSONException

class Notifications: CordovaPlugin() {
	companion object {
		private const val TAG = "pushNotification"

		@JvmStatic
		var lastTapedNotification = ""
	}
	lateinit var context: CallbackContext

	override fun pluginInitialize() {
		val activity = cordova.activity
		val extras = activity.intent.extras
		if (extras != null) {
			val payload = extras.getString("pushNotification")
			if (payload != null) {
				lastTapedNotification = payload
			}
		}

		super.pluginInitialize()
	}

	@Throws(JSONException::class)
	override fun execute(action: String, data: JSONArray, callbackContext: CallbackContext): Boolean {
		context = callbackContext
		var result = true
		try {
			when (action) {
				"registration" -> {
					cordova.threadPool.execute {
						getFirebaseToken()
					}
				}
				"tapped" -> {
					cordova.threadPool.execute {
						val res = lastTapedNotification
						lastTapedNotification = ""
						context.success(res)
					}
				}
				else -> {
					handleError("Invalid action")
					result = false
				}
			}
		} catch (e: Exception) {
			handleError(e.toString())
			result = false
		}

		return result
	}

	private fun getFirebaseToken() {
		FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
			val token = instanceIdResult.token
			Log.d("FIREBASE TOKEN", token)
			context.success(token)
		}
	}

	private fun handleError(errorMsg: String) {
		try {
			Log.e(TAG, errorMsg)
			context.error(errorMsg)
		} catch (e: Exception) {
			Log.e(TAG, e.toString())
		}
	}
}
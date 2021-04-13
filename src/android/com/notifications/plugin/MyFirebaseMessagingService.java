package com.notifications.plugin;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static org.apache.cordova.engine.SystemWebViewEngine.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
	@Override 
	public void onMessageReceived(RemoteMessage remoteMessage) {
	}
}
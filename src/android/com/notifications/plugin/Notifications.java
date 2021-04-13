package com.notifications.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.*;
import com.google.firebase.messaging.*;

import static android.provider.Settings.Global.getString;

public class Notifications extends CordovaPlugin {
  @Override
  public boolean execute(
    String action, 
    JSONArray args, 
    CallbackContext callbackContext
  ) throws JSONException {
    if ("registration".equals(action)) {
      FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
        @Override
        public void onComplete(@NonNull Task<String> task) {
          System.out.println(task.getResult());
          callbackContext.success(task.getResult());
        }
      });
      return true;
    } else {
      callbackContext.error("Permission denied");
    }
    return false;
  }
}
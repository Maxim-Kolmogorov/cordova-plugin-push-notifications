# Cordova Push Notification plugin

Simple plugin of Cordova for Push Notification in iOS and Android. You will be able to receive a device token and using Google services (Firebase Cloud Messages) and Apple (Apple Push Notification) to send push notifications.

# Install

```bash
cordova plugin add cordova-plugin-push-notifications
```

Or 

Downoload plugin in .zip arcive, unpack and:

```bash
cordova add plugin 'path/to/plugin/in/system'
```

And follow the instruction...

## iOS

Need Cordova iOS platform >= 5.0.0

Afther "plugin add" or "cordova build ios" command open iOS project in Xcode, go to Signing & Capabilities and click on "+" in left. Select Push Notification and lets generate SSL-certificate for push notification. Instruction see [here](https://developer.apple.com/documentation/usernotifications/setting_up_a_remote_notification_server/establishing_a_certificate-based_connection_to_apns).


## Android

Need Cordova Android platform >= 9.0.0

Afther "plugin add" command, add new preference in config.xml of project:

```xml
  <preference name="AndroidXEnabled" value="true" />
```

This preference need only for Android platform, example:

```xml
  <platform name="android">
    <preference name="AndroidXEnabled" value="true" />
  </platform>
```

Next, go to Firebase and registration your application in Push Notification service (Cloud Messages). Get in Firebase google-services.json and put this file in /platforms/android/app/. Where second build.gradle file and folder src.

You can automate this process. You can put google-services.json in the root of the Cordova project, and add an entry like this to config.xml:

```xml
  <resource-file src="res/google-services.json" target="app/google-services.json" />
```

Now, you can build the project via cordova build android.

## Attention

When the user receives a notification, the app icon is displayed in the status bar on iOS. Android does not provide this, so you need to add to AndroidManifest.xml:

```xml
 <meta-data android:name="com.google.firebase.messaging.default_notification_icon" android:resource="@drawable/my-icons" />
 ```

 @drawable/my-icons - is an android resource hand made in Android Studio. You can play around with the Cordova config.xml to automate this in the future and load the assets you want via:

 ```xml
  <resource-file src="res/icon" target="app/icon" />
  <config-file target="AndroidManifest.xml" parent="/manifest/application"> 
    <meta-data android:name="com.google.firebase.messaging.default_notification_icon" android:resource="@drawable/my-icons" />
  </config-file>
 ```

But it took me a long time, so doing it manually will be much easier (no more than 2 minutes).

See [here](https://stackoverflow.com/questions/37325051/notification-icon-with-the-new-firebase-cloud-messaging-system).

# Plugin API

JavaScript example:

```js
window.pushNotification.registration(
  (token) => {
    console.log(token);
  },
  (error) => {
    console.error(error);
  }
);
```

TypeScript example (with import of interface):

```ts
import PushNotification from 'cordova-plugin-push-notifications/types'

window.pushNotification.registration(
  (token: string) => {
    console.log(token);
  },
  (error: string) => {
    console.error(error)
  }
) as PushNotification
```

## iOS

If the user has allowed the sending notifications, then in the status you will receive a token, then do whatever you want: send the token to your server, or test sending push notifications through the [Pusher](https://github.com/noodlewerk/NWPusher) pod-file.

## Android

After receiving the token, you can test Push Notification directly from the Firebase Console.



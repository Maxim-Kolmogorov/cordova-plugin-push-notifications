# Cordova Push Notification plugin

Simple plugin of Cordova for Push Notification in iOS and Android. You will be able to receive a device token using Google services (Firebase Cloud Messages) and Apple (Apple Push Notification) to send push notifications.

# Install

```bash
cordova plugin add cordova-plugin-push-notifications
```

Or 

Downoload plugin in .zip arcive, unpack and:

```bash
cordova add plugin 'path/to/plugin/in/system'
```

And follow the instructions...

## Cordova

Required Cordova version >= 9.0.0

Check via:

```bash
cordova -v
```

## iOS

Required Cordova iOS platform >= 5.0.0

After entering "plugin add" or "cordova build ios" command open iOS project in Xcode, go to Signing & Capabilities and click on "+" in left. Select Push Notification and generate SSL-certificate for push notification. You can check the instructions [here](https://developer.apple.com/documentation/usernotifications/setting_up_a_remote_notification_server/establishing_a_certificate-based_connection_to_apns).


## Android

Required Cordova Android platform >= 9.0.0

After entering "plugin add" command, add new preference in config.xml of project:

```xml
<preference name="AndroidXEnabled" value="true" />
```

This preference is needed only for Android platform, example:

```xml
<platform name="android">
  <preference name="AndroidXEnabled" value="true" />
</platform>
```

Next, go to Firebase and register your application in Push Notification service (Cloud Messages). Get in Firebase google-services.json and put this file in /platforms/android/app/ (where second build.gradle file and folder src. are located).

You can automate this process by putting google-services.json in the root of the Cordova project and adding an entry like this to config.xml:

```xml
<resource-file src="res/google-services.json" target="app/google-services.json" />
```

Now, you can build the project via "cordova build android".

## Attention

When the user receives a notification, the app icon is displayed in the status bar on iOS. Android does not provide this. You have to add the following lines to AndroidManifest.xml:

```xml
<meta-data android:name="com.google.firebase.messaging.default_notification_color" android:value="0"/>
<meta-data android:name="com.google.firebase.messaging.default_notification_icon" android:resource="@drawable notification_icons" />
 ```

@drawable/notification_icons - is an android resource hand made in Android Studio. And the color is indicated in HEX, example: #fff (or 0 is the default color).

You can set your resource and color by adding this code to your AndroidManifest.xml across config.xml.

### How to create resource for Android notifications

You need to add this to AndroidManifest.xml

```xml
<meta-data android:name="com.google.firebase.messaging.default_notification_icon" android:resource="@drawable/my-icons" />
 ```

And create the resource itself in the Android studio. You can play around with the Cordova config.xml to automate this in the future and load the assets you want via:

 ```xml
<resource-file src="res/icon" target="app/drawable/my-icon" />
<config-file target="AndroidManifest.xml" parent="/manifest/application"> 
  <meta-data android:name="com.google.firebase.messaging.default_notification_icon" android:resource="@drawable/my-icon" />
</config-file>
 ```

See [here](https://stackoverflow.com/questions/37325051/notification-icon-with-the-new-firebase-cloud-messaging-system).

For clarity, I have attached my icon code from config.xml (from my own project):

 ```xml
<resource-file src="drawable/drawable-anydpi-v24/notification_icons.xml" target="app/src/main/res/drawable-anydpi-v24/notification_icons.xml" />
<resource-file src="drawable/drawable-hdpi/notification_icons.png" target="app/src/main/res/drawable-hdpi/notification_icons.png" />
<resource-file src="drawable/drawable-mdpi/notification_icons.png" target="app/src/main/res/drawable-mdpi/notification_icons.png" />
<resource-file src="drawable/drawable-xhdpi/notification_icons.png" target="app/src/main/res/drawable-xhdpi/notification_icons.png" />
<resource-file src="drawable/drawable-xxhdpi/notification_icons.png" target="app/src/main/res/drawable-xxhdpi/notification_icons.png" />
<config-file target="AndroidManifest.xml" parent="/manifest/application"> 
  <meta-data android:name="com.google.firebase.messaging.default_notification_icon" android:resource="@drawable/notification_icons" />
</config-file>
 ```

And finally, add this dependency in tag <widget\>:

 ```xml
<widget ... xmlns:android="schemas.android.com/apk/res/android" ...></widget>
 ```

The icons themselves can be downloaded from here, from my Google Drive. Download [icons](https://drive.google.com/file/d/1_RinnmvIvwx157cgjn_4cwn2GBcrYQVa/view?usp=sharing).


# Plugin API

JavaScript example:

```js
window.pushNotification.registration((token) => {
  console.log(token);
})

// Catch notification if app launched after user touched on message
window.pushNotification.tapped((payload) => {
  console.log(payload);
})
```

May be combined with the iOS "resume" [event](https://cordova.apache.org/docs/en/10.x/cordova/events/events.html#resume) when the notification was clicked in the background. On Android, such manipulations are not needed, because clicking on a notification will rerender screen (activity).

```kotlin
val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
  addNextIntentWithParentStack(resultIntent)
  getPendingIntent(101, PendingIntent.FLAG_CANCEL_CURRENT)
}
```

Responsible for this action FLAG_CANCEL_CURRENT. Using other flags did not lead to redrawing, but also did not update the data in Activity Extra. Maybe I can find a solution in the future.

The function "tapped" always returns an empty string. But, if there was a launch through a notification and there is a "payload" there, then it will give its contents.

It is obligatory to receive the "payload" data must be in the following form:

## iOS

```js
{ 
  aps: {
    alert: {
      title: "Hello Alex!",
      subtitle: "You pretty boy"
    },
    payload: "payload 1234"
  }
}
```

## Android

```js
{
  data: {
    title: "Hello Alex!", 
    body: "You pretty boy!", 
    payload: "payload 1234"
  },
  priority: "high",
  content_available: true
}
```

Importantly, do this without "notification".

# TypeScript

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

# How test

## iOS

If the user has allowed the sending notifications, then in the status you will receive a token, then do whatever you want: send the token to your server, or test sending push notifications through the [Knuff](https://github.com/KnuffApp/Knuff) app.

## Android

After receiving the token, you can test push notification directly from the Firebase Console.



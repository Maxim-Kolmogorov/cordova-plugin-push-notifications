import UserNotifications

// Notification info
var globalPushNotificationToken: String = ""
var globalTapedNotification: String = ""

// Thread
let PushNotificationQueue = DispatchQueue(label: "pluginPushNotificationQueue", attributes: .concurrent)
let PushNotificationSemaphore = DispatchSemaphore(value: 0)

// Extension AppDelegate
@objc(AppDelegate) extension AppDelegate {
  // Catch notification if app launched after user touched on message
  open override func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey : Any]?) -> Bool {
    if (launchOptions != nil) {
      if let userInfo = launchOptions?[UIApplication.LaunchOptionsKey.remoteNotification] as? [String : Any] {
        globalTapedNotification = self.parseAPSObject(obj: userInfo)
      }
    }

    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  // Register token
  open override func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    let tokenParts = deviceToken.map { data -> String in
      return String(format: "%02.2hhx", data)
    }
    let token = tokenParts.joined()
    globalPushNotificationToken = token
    PushNotificationSemaphore.signal()
  }

  // Token errors handler
  open override func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
    print("Failed to register: \(error)")
    PushNotificationSemaphore.signal()
  }

  // Catch notification in background (pause mode)
  open override func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any]) {
    globalTapedNotification = self.parseAPSObject(obj: userInfo as! [String : Any])
  }
    
  func parseAPSObject(obj: [String : Any]) -> String {
    let aps = obj["aps"] as? [String : Any]
    if let str = aps?["payload"] as? String {
      return str
    }
    return ""
  }
}

@objc(PushNotification)
class PushNotification: CDVPlugin {

  private var pushNotificationCallBackID = ""

  @objc(tapped:)
  private func tapped(command: CDVInvokedUrlCommand) {
    let res = globalTapedNotification
    globalTapedNotification = ""

    self.commandDelegate!.send(
      CDVPluginResult(
        status: CDVCommandStatus_OK,
        messageAs: res
      ),
      callbackId: command.callbackId
    )
  }
    
 @objc(registration:)
 private func registration(command: CDVInvokedUrlCommand) {
   self.pushNotificationCallBackID = command.callbackId
   self.registerForPushNotifications()
 }

  private func registerForPushNotifications() {
    UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) {
      (granted, error) in
      print("UserNotifications permission granted: \(granted)")

      guard granted else {
        self.pluginError()
        return
      }
      self.getNotificationSettings()
    }
  }

  private func getNotificationSettings() {
    UNUserNotificationCenter.current().getNotificationSettings { (settings) in
      print("UserNotifications settings: \(settings)")
      guard settings.authorizationStatus == .authorized else {
        self.pluginError()
        return
      }

      DispatchQueue.main.async {
        UIApplication.shared.registerForRemoteNotifications()
      }

      PushNotificationQueue.async {
        PushNotificationSemaphore.wait(timeout: .distantFuture)
        if (globalPushNotificationToken.count > 0) {
          self.pluginReady(token: globalPushNotificationToken)
        } else {
          self.pluginError()
        }
        PushNotificationSemaphore.signal()
      }
    }
  }

  private func pluginReady(token: String = "") {
    self.commandDelegate!.send(
      CDVPluginResult(
        status: CDVCommandStatus_OK,
        messageAs: token
      ),
      callbackId: self.pushNotificationCallBackID
    )
  }

  private func pluginError() {
    self.commandDelegate!.send(
      CDVPluginResult(
        status: CDVCommandStatus_ERROR,
        messageAs: "Permission denied"
      ),
      callbackId: self.pushNotificationCallBackID
    )
  }
}

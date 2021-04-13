import UserNotifications

var PushNotificationPluginToken = ""
let PushNotificationQueue = DispatchQueue(label: "pluginPushNotificationQueue", attributes: .concurrent)
let PushNotificationSemaphore = DispatchSemaphore(value: 0)

@objc(AppDelegate) extension AppDelegate {
  open override func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    let tokenParts = deviceToken.map { data -> String in
        return String(format: "%02.2hhx", data)
    }
    let token = tokenParts.joined()
    PushNotificationPluginToken = token
    PushNotificationSemaphore.signal()
  }

  open override func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
    print("Failed to register: \(error)")
    PushNotificationSemaphore.signal()
  }
}

@objc(PushNotification)
class PushNotification: CDVPlugin {

  private var PushNotificationCallBackID = ""

  @objc(registration:)
  private func registration(command: CDVInvokedUrlCommand) {
    self.PushNotificationCallBackID = command.callbackId
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
        if (PushNotificationPluginToken.count > 0) {
          self.pluginReady(token: PushNotificationPluginToken)
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
      callbackId: self.PushNotificationCallBackID
    )
  }

  private func pluginError() {
    self.commandDelegate!.send(
      CDVPluginResult(
        status: CDVCommandStatus_ERROR,
        messageAs: "Permission denied"
      ),
      callbackId: self.PushNotificationCallBackID
    )
  }
}
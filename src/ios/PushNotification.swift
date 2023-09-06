import UserNotifications

// Globale Variablen
var globalPushToken: String = ""
var globalNotificationTap: String = ""

@objc(PushNotificationPlugin)
class PushNotificationPlugin: CDVPlugin {

  private var callbackId: String!
  
  override func pluginInitialize() {
   // Notification Service Extension registrieren
   let serviceExtension = UNNotificationServiceExtension(identifier: "NotificationService", bundle: Bundle.main)
   UNUserNotificationCenter.current().add(serviceExtension)
  }

  // Token registrieren
  func registerForPush(command: CDVInvokedUrlCommand) {
    callbackId = command.callbackId
    UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
      if granted {
        self.getNotificationSettings()  
      } else {
        self.sendError("Permission denied")
      }
    }
  }

  // Notification Einstellungen abrufen
  func getNotificationSettings() {
    UNUserNotificationCenter.current().getNotificationSettings { settings in
      guard settings.authorizationStatus == .authorized else {
        self.sendError("Permission denied")
        return
      }
      DispatchQueue.main.async {
        UIApplication.shared.registerForRemoteNotifications() 
      }
    }
  }

  // Token registriert
  override func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    let token = deviceToken.reduce("", {$0 + String(format: "%02X", $1)})
    globalPushToken = token
    sendToken() 
  }

  // Token Error
  override func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
    sendError(error.localizedDescription)
  }

  // Notification in App geöffnet
  override func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {   
    if let aps = userInfo["aps"] as? [String: AnyObject], let alert = aps["alert"] as? String {
      globalNotificationTap = alert 
    }
    completionHandler(.newData)
  }

  // Notification im Hintergrund erhalten 
  override func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
    
    if let url = userInfo["attachment-url"] as? String { 
      downloadAttachment(url: url)
    }

    completionHandler(.newData)
  }

  // Anhang herunterladen
  func downloadAttachment(url: String) {
    let session = URLSession.shared
    let task = session.downloadTask(with: URL(string: url)!) { location, _, _ in
      guard let location = location else { return }
      let filename = url.components(separatedBy: "/").last!
      let destination = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        .appendingPathComponent(filename)

      try? FileManager.default.moveItem(at: location, to: destination) 
    }
    task.resume()
  }

  // Abgefangene Notification senden
  func sendTappedNotification(command: CDVInvokedUrlCommand) {
    let tapped = globalNotificationTap
    globalNotificationTap = ""
    commandDelegate.send(CDVPluginResult(status: .ok, messageAs: tapped), callbackId: command.callbackId)
  }

  // Token senden 
  func sendToken() {
    commandDelegate.send(CDVPluginResult(status: .ok, messageAs: globalPushToken), callbackId: callbackId)
  }
  
  // Fehler senden
  func sendError(_ error: String) {
    commandDelegate.send(CDVPluginResult(status: .error, messageAs: error), callbackId: callbackId) 
  }

}
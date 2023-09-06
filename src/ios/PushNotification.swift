import UserNotifications


class NotificationService: UNNotificationServiceExtension {
 
var contentHandler: ((UNNotificationContent) -> Void)?
var bestAttemptContent: UNMutableNotificationContent?
 
override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
    self.contentHandler = contentHandler
    bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)

    if let bestAttemptContent = bestAttemptContent {
        var urlString:String? = nil
        if let urlImageString = request.content.userInfo["media-attachment-url"] as? String {
            urlString = urlImageString
        }
         
        if urlString != nil, let fileUrl = URL(string: urlString!) {
            print("fileUrl: \(fileUrl)")
             
            // Download the attachment
            URLSession.shared.downloadTask(with: fileUrl) { (location, response, error) in
                if let location = location {
                    // Move temporary file to remove .tmp extension
                    if (error == nil) {
                        let tmpDirectory = NSTemporaryDirectory()
                        let tmpFile = "file://".appending(tmpDirectory).appending(fileUrl.lastPathComponent)
                        let tmpUrl = URL(string: tmpFile)!
                        try! FileManager.default.moveItem(at: location, to: tmpUrl)
                         
                        // Add the attachment to the notification content
                        if let attachment = try? UNNotificationAttachment(identifier: fileUrl.lastPathComponent, url: tmpUrl) {
                            bestAttemptContent.attachments = [attachment]
                            }
                    }
                    if(error != nil) {
                        print("Failed to download attachment: \(error.debugDescription)")
                    }
                }
                // Serve the notification content
                contentHandler(bestAttemptContent)
            }.resume()
        }
    }
}
 
override func serviceExtensionTimeWillExpire() {
    // Called just before the extension will be terminated by the system.
    // Use this as an opportunity to deliver your "best attempt" at modified content, otherwise the original push payload will be used.
    if let contentHandler = contentHandler, let bestAttemptContent = bestAttemptContent {
        contentHandler(bestAttemptContent)
    }
}

}

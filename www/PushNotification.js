var exec = require('cordova/exec')

exports.registration = function (success, error = function() {}) {
  if (typeof success != 'function') {
    console.error('pushNotification.registration() failure: success parameter must be a function')
    return
  }
  exec(success, error, 'PushNotification', 'registration')
}

exports.tapped = function (success, error = function() {}) {
  if (typeof success != 'function') {
    console.error('pushNotification.tapped() failure: success parameter must be a function')
    return
  }
  exec(success, error, 'PushNotification', 'tapped')
}

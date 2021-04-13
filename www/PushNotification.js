var exec = require('cordova/exec')

exports.registration = function(success, error) {
  if (error == null) {
    error = function() {}
  }

  if (typeof error != 'function') {
    console.error('PushNotification.registration failure: error parameter not a function');
    return
  }

  if (typeof success != 'function') {
    console.error('PushNotification.registration failure: success parameter must be a function');
    return
  }

  exec(success, error, 'PushNotification', 'registration')
};
# Changelog

## 4.1.1

* **SECURITY FIX**: Fix Intent Redirection vulnerability in Android implementation
* Migrate Android build files to Kotlin DSL (.kts)
* Update Android Gradle Plugin to 8.5.1
* Update Gradle to 8.7
* Update Kotlin to 1.9.23
* Update Google Play Services dependencies to latest versions
* Update AndroidX dependencies to latest versions
* Update compileSdk to 34
* Add Intent validation to prevent malicious Intent injection
* Add safe type casting to prevent ClassCastException attacks
* Improve error handling in broadcast receivers

## 4.1.0

* Fix `com.google.android.gms.common.api.ApiException: 16`


## 4.0.0

* Breaking: Upgrade Gradle version.
* Breaking: Upgrade Kotlin version.
* Breaking: Remove v1 embedding support.

## 3.0.4

* Correct logo position.

## 3.0.3

* Rebranding.

## 3.0.2

* Fix SmsRetrieverBroadcastReceiver on Android 14.

## 3.0.1

* Fix Android App crash on Android version 14.

## 3.0.0

* Breaking: Upgraded phone number hint API. Now may require updating Google Services on the device.
* Added base test case for package testing.
* Fix example of project.
* Fix Android App crash when phone allowed two SMS at the same time.

## 2.1.1

* Fixed example

## 2.1.0

* Change API: `startListenUserConsent` and `startListenRetriever` returns Future

## 2.0.0

* Static methods of OTPInteractor have been removed. Improved dependency passing.

## 1.1.0-dev.1

* Make native Android receivers null after unregister. (minor)
* Add `autoStop` param to `OTPTextEditController`. (minor)
* Make `OTPTextEditController`'s `onCodeReceive` non-required. (minor)

## 1.0.2

* Stable release

## 1.0.2-dev.1

* Apply new lint rules.

## 1.0.1

* Fix android build bug related on null-safety.
* Update readme.

## 1.0.0

* Migrate this package to null safety.

## 0.0.1-dev.4

* fix platformException on TimeOut

## 0.0.1 - Released

* add SMS Retriever API support
* add SMS User Consent API support
* add OTPStrategy for another OTP code input

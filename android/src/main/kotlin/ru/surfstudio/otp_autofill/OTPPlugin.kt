package ru.surfstudio.otp_autofill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.result.IntentSenderRequest.*
import androidx.annotation.NonNull;
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.phone.SmsRetriever

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/// Channel
const val channelName: String = "otp_surfstudio"

/// Requests
const val credentialPickerRequest = 1
const val smsConsentRequest = 2

/// Methods
const val getTelephoneHint: String = "getTelephoneHint"
const val startListenUserConsent: String = "startListenUserConsent"
const val startListenRetriever: String = "startListenRetriever"
const val stopListenForCode: String = "stopListenForCode"
const val getAppSignatureMethod: String = "getAppSignature"

/// Arguments
const val senderTelephoneNumber: String = "senderTelephoneNumber"

/** OtpTextEditControllerPlugin */
class OTPPlugin : FlutterPlugin, MethodCallHandler, PluginRegistry.ActivityResultListener,
    ActivityAware {

    private lateinit var channel: MethodChannel

    private var smsUserConsentBroadcastReceiver: SmsUserConsentReceiver? = null
    private var smsRetrieverBroadcastReceiver: SmsRetrieverReceiver? = null
    private var activity: Activity? = null
    private val request = GetPhoneNumberHintIntentRequest.builder().build()

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, channelName)
        channel.setMethodCallHandler(this);
    }
    private var context: Context? = null
    private var lastResult: Result? = null

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            startListenRetriever -> {
                listenRetriever(result)
            }

            startListenUserConsent -> {
                listenUserConsent(call, result)
            }

            getTelephoneHint -> {
                showNumberHint(result)
            }

            stopListenForCode -> {
                unRegisterBroadcastReceivers()
                result.success(true)
            }

            getAppSignatureMethod -> {
                if (activity != null) {
                    val signature = AppSignatureHelper(this.activity!!).getAppSignatures()[0]
                    result.success(signature)
                } else result.success(null)
            }

            else -> result.notImplemented()
        }
    }

    private fun showNumberHint(result: Result) {
        lastResult = result

        if (activity == null) return

        // if activity is not null will build 'show hint' intent
        // on success will start showing hint
        Identity.getSignInClient(activity!!)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener { res ->
                res.intentSender
                val request = Builder(res).build()

                activity!!.startIntentSenderForResult(
                    request.intentSender, credentialPickerRequest,
                    null, 0, 0, 0
                )
            }
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        /// when activity being replaced by another activity or destroyed - unregister receivers
        unRegisterBroadcastReceivers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        when (requestCode) {
            smsConsentRequest ->
                // Obtain the phone number from the result
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Get SMS message content
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    lastResult?.success(message)
                    lastResult = null
                } else {
                    // Consent denied. User can type OTC manually.
                }

            credentialPickerRequest -> if (resultCode == Activity.RESULT_OK && data != null) {
                // Check if the result is for credential picker
                if (data.hasExtra(SmsRetriever.EXTRA_SMS_MESSAGE)) {
                    // This is a result from the SMS consent picker
                    val phoneNumber =
                        Identity.getSignInClient(context!!).getPhoneNumberFromIntent(data)
                    lastResult?.success(phoneNumber)
                    lastResult = null
                } else {
                    lastResult?.error("403", "User denied consent", null)
                    lastResult = null
                }
            }
        }
        return true
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        binding.addActivityResultListener(this)
    }

    private fun listenUserConsent(@NonNull call: MethodCall, @NonNull result: Result) {
        val senderNumber = call.argument<String?>(senderTelephoneNumber)
        // Start listening for SMS User Consent broadcasts from senderPhoneNumber
        // The Task<Void> will be successful if SmsRetriever was able to start
        // SMS User Consent, and will error if there was an error starting.
        if (context != null) {
            lastResult = result
            val task = SmsRetriever.getClient(context!!).startSmsUserConsent(senderNumber)
            task.addOnSuccessListener { registerSmsUserConsentBroadcastReceiver() }
        }
    }

    private fun listenRetriever(@NonNull result: Result) {
        if (activity != null) {
            lastResult = result
            val client = SmsRetriever.getClient(activity!!)
            val task = client.startSmsRetriever()
            task.addOnSuccessListener { registerSmsRetrieverBroadcastReceiver() }
        }
    }

    private fun registerSmsUserConsentBroadcastReceiver() {
        smsUserConsentBroadcastReceiver = SmsUserConsentReceiver().also {
            it.smsBroadcastReceiverListener =
                object : SmsUserConsentReceiver.SmsUserConsentBroadcastReceiverListener {
                    override fun onSuccess(intent: Intent?) {
                        intent?.let { consentIntent ->
                            // Validate that the intent is from Google Play Services
                            if (isIntentFromGooglePlayServices(consentIntent)) {
                                try {
                                    activity?.startActivityForResult(
                                        consentIntent,
                                        smsConsentRequest
                                    )
                                } catch (e: Exception) {
                                    // Handle potential security exceptions
                                    lastResult?.error("SECURITY_ERROR", "Failed to start consent activity: ${e.message}", null)
                                    lastResult = null
                                }
                            } else {
                                // Reject untrusted intent
                                lastResult?.error("SECURITY_ERROR", "Received untrusted intent", null)
                                lastResult = null
                            }
                        }
                    }

                    override fun onFailure() {
                        lastResult?.error("408", "Timeout exception", null)
                        lastResult = null
                    }
                }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.activity?.registerReceiver(
                smsUserConsentBroadcastReceiver,
                intentFilter,
                SmsRetriever.SEND_PERMISSION,
                null,
                Context.RECEIVER_EXPORTED,
            )
        } else {
            this.activity?.registerReceiver(
                smsUserConsentBroadcastReceiver,
                intentFilter,
                SmsRetriever.SEND_PERMISSION,
                null
            )
        }
    }

    private fun registerSmsRetrieverBroadcastReceiver() {
        smsRetrieverBroadcastReceiver = SmsRetrieverReceiver().also {
            it.smsBroadcastReceiverListener =
                object : SmsRetrieverReceiver.SmsRetrieverBroadcastReceiverListener {
                    override fun onSuccess(sms: String?) {
                        sms?.let { it ->
                            lastResult?.success(it)
                            lastResult = null
                        }
                    }

                    override fun onFailure() {
                        lastResult?.error("408", "Timeout exception", null)
                        lastResult = null
                    }
                }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.activity?.registerReceiver(
                smsRetrieverBroadcastReceiver,
                intentFilter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            this.activity?.registerReceiver(
                smsRetrieverBroadcastReceiver, intentFilter,
            )
        }
    }

    private fun unRegisterBroadcastReceivers() {
        if (smsUserConsentBroadcastReceiver != null) {
            activity?.unregisterReceiver(smsUserConsentBroadcastReceiver)
            smsUserConsentBroadcastReceiver = null
        }
        if (smsRetrieverBroadcastReceiver != null) {
            activity?.unregisterReceiver(smsRetrieverBroadcastReceiver)
            smsRetrieverBroadcastReceiver = null
        }
    }

    override fun onDetachedFromActivity() {
        unRegisterBroadcastReceivers()
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    /**
     * Validates that an Intent is from Google Play Services.
     * This helps prevent Intent Redirection vulnerabilities.
     */
    private fun isIntentFromGooglePlayServices(intent: Intent): Boolean {
        // Check if the intent has a component (package and class)
        val component = intent.component
        if (component != null) {
            // Verify the package name is from Google Play Services
            val packageName = component.packageName
            return packageName == "com.google.android.gms" || 
                   packageName == "com.google.android.gsf"
        }
        
        // If no component is specified, check the action
        // SMS consent intents should have specific actions
        val action = intent.action
        return action == "com.google.android.gms.auth.api.phone.SMS_CONSENT" ||
               action == "com.google.android.gms.auth.api.phone.SMS_RETRIEVED"
    }
}

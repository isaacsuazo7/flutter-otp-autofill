package ru.surfstudio.otp_autofill

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsUserConsentReceiver : BroadcastReceiver() {

    lateinit var smsBroadcastReceiverListener: SmsUserConsentBroadcastReceiverListener

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {

            val extras = intent.extras
            
            // Safely extract the status to avoid ClassCastException
            val statusObj = extras?.get(SmsRetriever.EXTRA_STATUS)
            if (statusObj !is Status) {
                // Invalid status object, likely a malicious intent
                return
            }
            
            val smsRetrieverStatus = statusObj

            when (smsRetrieverStatus.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Extract the consent intent safely
                    try {
                        val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        if (consentIntent != null) {
                            smsBroadcastReceiverListener.onSuccess(consentIntent)
                        }
                    } catch (e: Exception) {
                        // Failed to extract consent intent, possibly malicious
                        // Do not propagate the error to avoid exposing internals
                    }
                }

                CommonStatusCodes.TIMEOUT -> {
                    try{
                        smsBroadcastReceiverListener.onFailure()
                    } catch (e: Exception) {
                        ///DO NOTHING, end listening
                    }
                }
            }
        }
    }

    interface SmsUserConsentBroadcastReceiverListener {
        fun onSuccess(intent: Intent?)
        fun onFailure()
    }
}
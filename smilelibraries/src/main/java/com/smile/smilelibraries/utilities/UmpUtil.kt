package com.smile.smilelibraries.utilities

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object UmpUtil {

    interface UmpInterface {
        fun callback()
    }

    private const val TAG = "UmpUtil"
    // Variable to hold the ConsentInformation instance.
    private lateinit var consentInformation: ConsentInformation
    // Variable to hold the ConsentForm instance.
    // This can be null if no form is available or needed.
    // private var consentForm: ConsentForm? = null
    // Helper variable to determine if the privacy options form is required.
    private val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun canRequestAds(): Boolean {
        if (::consentInformation.isInitialized) {
            Log.i(TAG, "canRequestAds().consentInformation is not null")
            return consentInformation.canRequestAds()
        } else {
            Log.i(TAG, "canRequestAds().consentInformation is null")
            return true
        }
    }

    fun getConsentStatus(): Int {
        if (::consentInformation.isInitialized) {
            Log.i(TAG, "canRequestAds().consentInformation is not null")
            return consentInformation.consentStatus
        } else {
            Log.i(TAG, "canRequestAds().consentInformation is null")
            return ConsentInformation.ConsentStatus.UNKNOWN
        }
    }

    // Initialize the UMP SDK and request a consent info update.
    fun initConsentInformation(activity: Activity,
                               geography: Int,
                               deviceHashedId: String,
                               ump: UmpInterface) {
        Log.i(TAG, "initConsentInformation()")
        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        myRequestConsentInfoUpdate(activity, geography, deviceHashedId, ump)
    }

    /**
     * Requests an update to the user's consent information.
     */
    private fun myRequestConsentInfoUpdate(activity: Activity,
                                           geography: Int,
                                           deviceHashedId: String,
                                           ump: UmpInterface) {
        Log.i(TAG, "myRequestConsentInfoUpdate()")
        // release version
        var params = ConsentRequestParameters.Builder().build()
        //
        if (!deviceHashedId.isEmpty()) {
            // debug test version of Samsung A10 S
            // Optional: Add debug settings for testing with test devices.
            // geography = DEBUG_GEOGRAPHY_EEA
            // deviceHashedId = "8F6C5B0830E624E8D8BFFB5853B4EDDD"
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(geography)
                .addTestDeviceHashedId(deviceHashedId).build()
            params = ConsentRequestParameters.Builder()
                .setConsentDebugSettings(debugSettings).build()
            RequestConfiguration.Builder().setTestDeviceIds(listOf(deviceHashedId))
        }
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // On successful update, load and show the consent form if required.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity)
                { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent gathering failed: " +
                                "${formError.errorCode}: ${formError.message}")
                    } else {
                        Log.i(TAG, "Consent gathering succeeded.")
                    }
                    // (Optional) Check consentInformation.privacyOptionsRequirementStatus
                    // after the update
                    // to show/hide a "Privacy options" button in your settings if needed.
                    // showPrivacyOptionsForm(activity, ump) OR
                    ump.callback()
                }
            },
            { requestConsentError ->
                // On error updating consent, log the error and check if ads can still be requested based on previous consent.
                Log.w(TAG, "Error updating consent information: " +
                        "${requestConsentError.errorCode}: ${requestConsentError.message}")
                // (Optional) Check consentInformation.privacyOptionsRequirementStatus
                // after the update
                // to show/hide a "Privacy options" button in your settings if needed.
                // showPrivacyOptionsForm(activity, ump) OR
                ump.callback()
            }
        )
    }

    /**
     * Presents the privacy options form.
     */
    private fun showPrivacyOptionsForm(activity: Activity, ump: UmpInterface) {
        Log.i(TAG, "showPrivacyOptionsForm().isPrivacyOptionsRequired = $isPrivacyOptionsRequired")
        if (isPrivacyOptionsRequired) {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null) {
                    Log.e(TAG, "Error showing privacy options form: " +
                                "${formError.message}")
                } else {
                    Log.i(TAG, "showing privacy options form")
                }
                ump.callback()
            }
        } else {
            ump.callback()
        }
    }
}
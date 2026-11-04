package com.example.util

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.example.data.model.isUserAdminEmail
import java.util.Locale
import java.util.TimeZone

object NigeriaRegionValidator {

    /**
     * Checks if the device appears to be in Nigeria (country code NG, Lagos timezone, or Nigerian SIM/network).
     * If [userEmail] belongs to an administrator, always returns true to allow administrative access.
     */
    fun isAllowedInNigeria(context: Context, userEmail: String? = null, nigeriaRestrictionActive: Boolean = true): Boolean {
        // Admins can always access from anywhere
        if (isUserAdminEmail(userEmail)) {
            return true
        }

        // If admin disabled the Nigeria-only restriction via remote config
        if (!nigeriaRestrictionActive) {
            return true
        }

        try {
            // Check 1: Device Locale
            val localeCountry = Locale.getDefault().country
            if (localeCountry.equals("NG", ignoreCase = true)) {
                return true
            }

            // Check 2: Timezone (Nigeria is in Africa/Lagos or WAT timezone)
            val timeZoneId = TimeZone.getDefault().id
            if (timeZoneId.contains("Lagos", ignoreCase = true) ||
                timeZoneId.contains("Africa/Lagos", ignoreCase = true) ||
                timeZoneId.contains("Nigeria", ignoreCase = true)
            ) {
                return true
            }

            // Check 3: Telephony Manager (SIM or Network Country ISO)
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simCountry = telephonyManager?.simCountryIso
            val networkCountry = telephonyManager?.networkCountryIso

            if (!simCountry.isNullOrBlank() && simCountry.equals("ng", ignoreCase = true)) {
                return true
            }
            if (!networkCountry.isNullOrBlank() && networkCountry.equals("ng", ignoreCase = true)) {
                return true
            }
        } catch (_: Exception) {}

        // For local development / emulator environments without SIM card or specific locale,
        // we check if the user is authenticated as an admin or if device locale or timezone matches.
        return false
    }
}

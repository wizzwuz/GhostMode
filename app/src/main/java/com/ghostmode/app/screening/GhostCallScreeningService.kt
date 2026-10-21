package com.ghostmode.app.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import com.ghostmode.app.GhostModeApp

/**
 * Telecom CallScreeningService that intercepts incoming calls from selected contacts
 * and rejects or silences them to make the device appear unavailable.
 */
class GhostCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return

        val app = applicationContext as? GhostModeApp ?: return
        val state = app.graph.state
        if (!state.ghostContactsEnabled.value) return

        val handle = callDetails.handle ?: return
        val rawNumber = handle.schemeSpecificPart.orEmpty()
        if (rawNumber.isEmpty()) return

        if (state.isNumberBlocked(rawNumber)) {
            val silenceOnly = state.ghostContactsSilenceOnly.value
            val logCalls = state.ghostContactsLogCalls.value

            val response = CallResponse.Builder()
                .setDisallowCall(true)
                .apply {
                    if (silenceOnly) {
                        setSilenceCall(true)
                    } else {
                        setRejectCall(true)
                    }
                }
                .setSkipNotification(true)
                .setSkipCallLog(!logCalls)
                .build()

            respondToCall(callDetails, response)
        }
    }
}

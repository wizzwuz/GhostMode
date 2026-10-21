package com.ghostmode.app.data

import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

/**
 * A contact or phone number that the user has selected to be unavailable to.
 */
data class GhostContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val enabled: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put(KEY_ID, id)
        put(KEY_NAME, name)
        put(KEY_PHONE, phoneNumber)
        put(KEY_ENABLED, enabled)
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phoneNumber"
        private const val KEY_ENABLED = "enabled"

        fun fromJson(json: JSONObject): GhostContact? = try {
            GhostContact(
                id = json.optString(KEY_ID).ifEmpty { UUID.randomUUID().toString() },
                name = json.getString(KEY_NAME),
                phoneNumber = json.getString(KEY_PHONE),
                enabled = json.optBoolean(KEY_ENABLED, true)
            )
        } catch (_: JSONException) {
            null
        }
    }
}

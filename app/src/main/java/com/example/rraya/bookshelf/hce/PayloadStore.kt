package com.example.rraya.bookshelf.hce

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.hceDataStore: DataStore<Preferences> by preferencesDataStore(name = "hce_demo")

class PayloadStore(private val appContext: Context) {
    private val store = appContext.hceDataStore

    private val keyName = stringPreferencesKey("payload_name")
    private val keyFormat = stringPreferencesKey("payload_format")
    private val keyBytesB64 = stringPreferencesKey("payload_bytes_b64")

    val activePayloadFlow: Flow<ActivePayload?> =
        store.data.map { prefs ->
            val name = prefs[keyName] ?: return@map null
            val format = (prefs[keyFormat] ?: return@map null).toPayloadFormatOrNull() ?: return@map null
            val b64 = prefs[keyBytesB64] ?: return@map null

            val bytes = try {
                Base64.decode(b64, Base64.DEFAULT)
            } catch (_: IllegalArgumentException) {
                return@map null
            }

            ActivePayload(name = name, format = format, bytes = bytes)
        }

    suspend fun setActivePayload(payload: ActivePayload) {
        store.edit { prefs ->
            prefs[keyName] = payload.name
            prefs[keyFormat] = payload.format.name
            prefs[keyBytesB64] = Base64.encodeToString(payload.bytes, Base64.NO_WRAP)
        }
    }

    private fun String.toPayloadFormatOrNull(): PayloadFormat? =
        try {
            PayloadFormat.valueOf(this)
        } catch (_: IllegalArgumentException) {
            null
        }
}


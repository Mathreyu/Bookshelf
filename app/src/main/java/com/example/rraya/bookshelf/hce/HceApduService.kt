package com.example.rraya.bookshelf.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HceApduService : HostApduService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var cachedPayload: ActivePayload? = null

    override fun onCreate() {
        super.onCreate()
        val store = PayloadStore(applicationContext)
        scope.launch {
            store.activePayloadFlow.collect { cachedPayload = it }
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        val apdu = commandApdu ?: return Apdu.SW_WRONG_LENGTH
        val payloadBytes = cachedPayload?.bytes
        return Apdu.handle(apdu, payloadBytes)
    }

    override fun onDeactivated(reason: Int) {
        // No-op. Kept for future logging if needed.
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}


package com.example.rraya.bookshelf.hce

import java.util.zip.CRC32

object Apdu {
    // Demo AID (must match res/xml/hce_apdu_service.xml)
    val AID: ByteArray = hexToBytes("F0010203040506")

    // Status words
    val SW_OK: ByteArray = byteArrayOf(0x90.toByte(), 0x00.toByte())
    val SW_FILE_NOT_FOUND: ByteArray = byteArrayOf(0x6A.toByte(), 0x82.toByte())
    val SW_WRONG_LENGTH: ByteArray = byteArrayOf(0x67.toByte(), 0x00.toByte())
    val SW_WRONG_PARAMS: ByteArray = byteArrayOf(0x6A.toByte(), 0x86.toByte())
    val SW_OUT_OF_RANGE: ByteArray = byteArrayOf(0x6B.toByte(), 0x00.toByte())
    val SW_CONDITIONS_NOT_SATISFIED: ByteArray = byteArrayOf(0x69.toByte(), 0x85.toByte())
    val SW_INS_NOT_SUPPORTED: ByteArray = byteArrayOf(0x6D.toByte(), 0x00.toByte())

    // Demo instructions
    const val INS_GET_INFO: Int = 0x10
    const val INS_READ_CHUNK: Int = 0x20

    fun isSelectAid(apdu: ByteArray): Boolean {
        // SELECT by AID: 00 A4 04 00 Lc [AID] (Le optional)
        if (apdu.size < 5) return false
        if (u8(apdu[0]) != 0x00) return false
        if (u8(apdu[1]) != 0xA4) return false
        if (u8(apdu[2]) != 0x04) return false
        if (u8(apdu[3]) != 0x00) return false

        val lc = u8(apdu[4])
        if (lc <= 0) return false
        if (apdu.size < 5 + lc) return false

        val aid = apdu.copyOfRange(5, 5 + lc)
        return aid.contentEquals(AID)
    }

    fun handle(apdu: ByteArray, payload: ByteArray?): ByteArray {
        if (isSelectAid(apdu)) return SW_OK

        if (apdu.size < 4) return SW_WRONG_LENGTH

        val cla = u8(apdu[0])
        val ins = u8(apdu[1])

        if (cla != 0x80) return SW_INS_NOT_SUPPORTED

        return when (ins) {
            INS_GET_INFO -> handleGetInfo(payload)
            INS_READ_CHUNK -> handleReadChunk(apdu, payload)
            else -> SW_INS_NOT_SUPPORTED
        }
    }

    private fun handleGetInfo(payload: ByteArray?): ByteArray {
        if (payload == null) return SW_CONDITIONS_NOT_SATISFIED

        val crc = CRC32().apply { update(payload) }.value.toInt()
        val len = payload.size

        // version(1) + length(4) + crc32(4)
        val out = ByteArray(1 + 4 + 4 + 2)
        out[0] = 0x01
        writeInt32BE(out, 1, len)
        writeInt32BE(out, 5, crc)
        out[out.size - 2] = SW_OK[0]
        out[out.size - 1] = SW_OK[1]
        return out
    }

    private fun handleReadChunk(apdu: ByteArray, payload: ByteArray?): ByteArray {
        if (payload == null) return SW_CONDITIONS_NOT_SATISFIED

        // Custom APDU:
        // 80 20 P1 P2 Lc(=6) [offset:4 bytes BE][length:2 bytes BE]
        if (apdu.size < 5) return SW_WRONG_LENGTH
        val lc = u8(apdu[4])
        if (lc != 6) return SW_WRONG_PARAMS
        if (apdu.size < 5 + lc) return SW_WRONG_LENGTH

        val data = apdu.copyOfRange(5, 11)
        val offset = readInt32BE(data, 0)
        val length = readUInt16BE(data, 4)

        if (offset < 0) return SW_WRONG_PARAMS
        if (length <= 0) return SW_WRONG_PARAMS

        // Keep chunks reasonably small for reader compatibility
        val cappedLen = minOf(length, 1024)

        if (offset > payload.size) return SW_OUT_OF_RANGE
        val end = offset + cappedLen
        if (end > payload.size) return SW_OUT_OF_RANGE

        val chunk = payload.copyOfRange(offset, end)
        return chunk + SW_OK
    }

    private fun u8(b: Byte): Int = b.toInt() and 0xFF

    private fun readInt32BE(bytes: ByteArray, offset: Int): Int {
        return (u8(bytes[offset]) shl 24) or
            (u8(bytes[offset + 1]) shl 16) or
            (u8(bytes[offset + 2]) shl 8) or
            u8(bytes[offset + 3])
    }

    private fun readUInt16BE(bytes: ByteArray, offset: Int): Int {
        return (u8(bytes[offset]) shl 8) or u8(bytes[offset + 1])
    }

    private fun writeInt32BE(out: ByteArray, offset: Int, value: Int) {
        out[offset] = ((value ushr 24) and 0xFF).toByte()
        out[offset + 1] = ((value ushr 16) and 0xFF).toByte()
        out[offset + 2] = ((value ushr 8) and 0xFF).toByte()
        out[offset + 3] = (value and 0xFF).toByte()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val cleaned = hex.replace(Regex("\\s+"), "").uppercase()
        require(cleaned.length % 2 == 0) { "Hex string must have even length" }
        val out = ByteArray(cleaned.length / 2)
        for (i in out.indices) {
            val idx = i * 2
            out[i] = cleaned.substring(idx, idx + 2).toInt(16).toByte()
        }
        return out
    }
}


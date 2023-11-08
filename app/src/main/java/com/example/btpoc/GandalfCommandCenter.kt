package com.example.btpoc

import android.os.ParcelUuid

val TX_CHARACTERISTIC = ParcelUuid.fromString("c991e031-812f-4eb5-a314-8b51a7754c39")!!
val RX_CHARACTERISTIC = ParcelUuid.fromString("c991e032-812f-4eb5-a314-8b51a7754c39")!!

class GandalfCommandCenter {
    companion object {
        private val PRODUCT_ID = byteArrayOf(0x00, 0x00, 0x00, 0x01)//"0x00000001".upperCaseHexStringToByteArray()
        private val  FRAME_COUNTER = byteArrayOf(0x00, 0x00, 0x00, 0x01)
        private val COMMAND_CODE = byteArrayOf(0x06, 0x04)
        private val APP_DATA_LENGTH = byteArrayOf(0x06, 0x02)//"0x0002".upperCaseHexStringToByteArray()
        private val APP_DATA = byteArrayOf(0x11, 0x25)//"0x1125".upperCaseHexStringToByteArray()

        fun getCommand(): ByteArray {
            val ts = getCurrentTimestampHex().upperCaseHexStringToByteArray()
            return PRODUCT_ID + FRAME_COUNTER + ts + COMMAND_CODE + APP_DATA_LENGTH + APP_DATA
        }


        private fun getCurrentTimestampHex(): String {
            // Get the current timestamp in seconds since the Unix epoch
            val timestamp = System.currentTimeMillis() / 1000
            // Convert the timestamp to a hexadecimal string
            return String.format("%x", timestamp)
        }
    }
}

val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex(lowercase: Boolean = false): String {
    val result = StringBuilder(size / 2)
    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }
    val uppercaseHex = result.toString()
    return if (lowercase) uppercaseHex.lowercase() else uppercaseHex
}

fun String.upperCaseHexStringToByteArray(): ByteArray {

    val result = ByteArray(length / 2)

    for (i in 0 until length step 2) {
        val firstIndex = HEX_CHARS.indexOf(this[i])
        val secondIndex = HEX_CHARS.indexOf(this[i + 1])
        result[i.shr(1)] = ((firstIndex shl 4) or secondIndex).toByte()
    }

    return result
}

const val FF = 0xFF.toByte()
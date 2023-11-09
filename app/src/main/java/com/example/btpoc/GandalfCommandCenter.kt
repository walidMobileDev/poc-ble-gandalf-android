package com.example.btpoc

import android.os.ParcelUuid
import androidx.compose.ui.text.toUpperCase

val TX_CHARACTERISTIC = ParcelUuid.fromString("c991e031-812f-4eb5-a314-8b51a7754c39")!!
val RX_CHARACTERISTIC = ParcelUuid.fromString("c991e032-812f-4eb5-a314-8b51a7754c39")!!
val SAMSUNG_CHARACTERISTIC = ParcelUuid.fromString("a7a48311-19c6-491b-aea6-7ea92b8f043a")!!
val SAMSUNG_NOTIF =  ParcelUuid.fromString("a7a48322-19c6-491b-aea6-7ea92b8f043a")!!

class GandalfCommandCenter {
    companion object {
        private val FRAME_LENGTH = byteArrayOf(0x00, 0x24)
        private val SOURCE_ID = byteArrayOf(0x00,0x1A, 0x21, 0xA2.toByte(), 0x78, 0xBE.toByte())
        private val DESTINATION_ID = byteArrayOf(0xD3.toByte(), 0x89.toByte(), 0xF2.toByte(), 0x04.toByte(), 0x19, 0x4C.toByte())//"0x00000001".upperCaseHexStringToByteArray() //1A21A278BE
        private val SOURCE_PORT = byteArrayOf(0x00, 0x00)//"0x00000001".upperCaseHexStringToByteArray() //1A21A278BE
        private val DESTINATION_PORT = byteArrayOf(0x00, 0x00)//"0x00000001".upperCaseHexStringToByteArray() //1A21A278BE
        private val PRODUCT_ID = byteArrayOf(0xD3.toByte(), 0x89.toByte(), 0xF2.toByte(), 0x04.toByte(), 0x19, 0x4C.toByte())//"0x00000001".upperCaseHexStringToByteArray() //1A21A278BE
        private val FRAME_COUNTER = byteArrayOf(0x00, 0x00, 0x00, 0x01)//"0x00000001".upperCaseHexStringToByteArray()
        private val COMMAND_CODE = byteArrayOf(0x0A, 0x08)//"0x0604".upperCaseHexStringToByteArray()
        private val APP_DATA_LENGTH = byteArrayOf(0x00, 0x00)//"0x0602".upperCaseHexStringToByteArray()
        //private val APP_DATA = byteArrayOf(0x11, 0x25)//"0x1125".upperCaseHexStringToByteArray()

        fun getCommand(): ByteArray {
            val ts = getCurrentTimestampHex().upperCaseHexStringToByteArray()
            return FRAME_LENGTH + SOURCE_ID + SOURCE_PORT + DESTINATION_ID + DESTINATION_PORT + PRODUCT_ID + FRAME_COUNTER + ts + COMMAND_CODE + APP_DATA_LENGTH //+ APP_DATA
        }

        fun getFramesLength(): ByteArray {
            return byteArrayOf(0xFF.toByte())
        }

        private fun getCurrentTimestampHex(): String {
            // Get the current timestamp in seconds since the Unix epoch
            val timestamp = System.currentTimeMillis() / 1000
            // Convert the timestamp to a hexadecimal string
            return String.format("%x", timestamp).toUpperCase()
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
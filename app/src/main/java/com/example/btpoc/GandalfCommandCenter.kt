package com.example.btpoc

import android.os.ParcelUuid
import android.util.Log

val TX_CHARACTERISTIC = ParcelUuid.fromString("c991e031-812f-4eb5-a314-8b51a7754c39")!!
val RX_CHARACTERISTIC = ParcelUuid.fromString("c991e032-812f-4eb5-a314-8b51a7754c39")!!
val SAMSUNG_CHARACTERISTIC = ParcelUuid.fromString("a7a48311-19c6-491b-aea6-7ea92b8f043a")!!
val SAMSUNG_NOTIF = ParcelUuid.fromString("a7a48322-19c6-491b-aea6-7ea92b8f043a")!!

object GandalfCommandCenter {
    private val FRAME_LENGTH = byteArrayOf(0x00, 0x24)
    private val SOURCE_ID = byteArrayOf(0x00, 0x1A, 0x21, 0xA2.toByte(), 0x78, 0xBE.toByte())
    private val DESTINATION_ID = byteArrayOf(
        0xD3.toByte(),
        0x89.toByte(),
        0xF2.toByte(),
        0x04.toByte(),
        0x19,
        0x4C.toByte()
    )//"0x00000001".upperCaseHexStringToByteArray() //1A21A278BE
    private val SOURCE_PORT =
        byteArrayOf(0x00, 0x00)//"0x00000001".upperCaseHexStringToByteArray() //1A21A278BE
    private val DESTINATION_PORT =
        byteArrayOf(0x00, 0x00)//"0x00000001".upperCaseHexStringToByteArray() //1A21A278BE
    private val PRODUCT_ID = byteArrayOf(
        0xD3.toByte(),
        0x89.toByte(),
        0xF2.toByte(),
        0x04.toByte(),
        0x19,
        0x4C.toByte()
    )//"0x00000001".upperCaseHexStringToByteArray() //1A21A278BE
    private val FRAME_COUNTER =
        byteArrayOf(0x00, 0x00, 0x00, 0x01)//"0x00000001".upperCaseHexStringToByteArray()
    private val COMMAND_CODE = byteArrayOf(0x0A, 0x08)//"0x0604".upperCaseHexStringToByteArray()
    private val APP_DATA_LENGTH = byteArrayOf(0x00, 0x00)//"0x0602".upperCaseHexStringToByteArray()
    //private val APP_DATA = byteArrayOf(0x11, 0x25)//"0x1125".upperCaseHexStringToByteArray()

    private const val APPLICATION_REQUEST_CMD =
        "24001123B100000100001123b10000010000A124E20000010100000054c8141a11060000"
    private const val GET_FIRMWARE_INFO_CMD =
        "24001123B100000100001123b10000010000A124E20000010100000054c8141a07070000"
    private const val REBOOT_CMD =
        "24001123B100000100001123b10000010000A124E20000010100000024c8141a0c010000"
    private const val GET_BATTERY_LEVEL_CMD =
        "24001123B100000100001123b10000010000A124E20000010100000054c8141a0080a000"
    private const val GET_PROXIMITY_VOLTAGE_STATUS_CMD =
        "24001123B100000100001123b10000010000A124E20000010100000054c8141a00d0a000"
    private const val GET_LAMP_STATUS_CMD =
        "24001123B100000100001123b10000010000A124E20000010100000054c8141a00a0a000"

    fun getCommand(): ByteArray {
        val ts = getCurrentTimestampHex().upperCaseHexStringToByteArray()
        return FRAME_LENGTH + SOURCE_ID + SOURCE_PORT + DESTINATION_ID + DESTINATION_PORT + PRODUCT_ID + FRAME_COUNTER + ts + COMMAND_CODE + APP_DATA_LENGTH //+ APP_DATA
    }

    fun applicationRequestCommand() = addTimestampToFrame(APPLICATION_REQUEST_CMD)

    fun getFirmwareInfoCommand() = addTimestampToFrame(GET_FIRMWARE_INFO_CMD)

    fun rebootCommand() = addTimestampToFrame(REBOOT_CMD)

    fun getBatteryLevelCommand() = addTimestampToFrame(GET_BATTERY_LEVEL_CMD)

    fun getVoltageLevelCommand() = addTimestampToFrame(GET_PROXIMITY_VOLTAGE_STATUS_CMD)

    fun getLampStatus() = addTimestampToFrame(GET_LAMP_STATUS_CMD)

    private fun addTimestampToFrame(frame: String): ByteArray {
        val formattedTimestamp = getCurrentTimestampHex().formatFrameForHighBytesFirst()

        return frame.replaceRange(startIndex = 56, endIndex = 64, formattedTimestamp)
            .uppercase().upperCaseHexStringToByteArray()
    }


    fun getFramesLength() = byteArrayOf(0xFF.toByte())

    private fun getCurrentTimestampHex(): String {
        // Get the current timestamp in seconds since the Unix epoch
        val timestamp = System.currentTimeMillis() / 1000
        // Convert the timestamp to a hexadecimal string
        return String.format("%x", timestamp).uppercase()
    }

    private fun getTimestampFromFrame(frame: String): String = frame.substring(56,64).formatFrameForLowBytesFirst()

    fun formatCatSafeFrame(data: ByteArray): String {
        val stringData = data.toHex()
        val result = StringBuilder()

        val frameLength = stringData.substring(startIndex = 0, endIndex = 4).formatFrameForLowBytesFirst()
        // Log.d("Walid","formatCatSafeApplicativeData frameLength : $frameLength")
        result.append("frameLenght : $frameLength\n")

        val sourceId = stringData.substring(startIndex = 4, endIndex = 16)
        // Log.d("Walid","formatCatSafeApplicativeData sourceId : $sourceId")
        result.append("sourceId : ${sourceId.formatFrameForLowBytesFirst()}\n")

        val sourcePort = stringData.substring(startIndex = 16, endIndex = 20)
        // Log.d("Walid","formatCatSafeApplicativeData sourcePort : $sourcePort")
        result.append("sourcePort : ${sourcePort.formatFrameForLowBytesFirst()}\n")

        val destId = stringData.substring(startIndex = 20, endIndex = 32)
        // Log.d("Walid","formatCatSafeApplicativeData destId : $destId")
        result.append("destId : ${destId.formatFrameForLowBytesFirst()}\n")

        val destPort = stringData.substring(startIndex = 32, endIndex = 36)
        // Log.d("Walid","formatCatSafeApplicativeData destPort : $destPort")
        result.append("destPort : ${destPort.formatFrameForLowBytesFirst()}\n")

        val productId = stringData.substring(startIndex = 36, endIndex = 48)
        // Log.d("Walid","formatCatSafeApplicativeData productId : $productId")
        result.append("productId : ${productId.formatFrameForLowBytesFirst()}\n")

        val frameCounter = stringData.substring(startIndex = 48, endIndex = 56)
        // Log.d("Walid","formatCatSafeApplicativeData frameCounter : $frameCounter")
        result.append("frameCounter : ${frameCounter.formatFrameForLowBytesFirst()}\n")

        val timestampRaw = stringData.substring(startIndex = 56, endIndex = 64)
        // Log.d("Walid","formatCatSafeApplicativeData timestampRaw : $timestampRaw")

        val timestamp = getTimestampFromFrame(frame = stringData).formatFrameForLowBytesFirst()
        // Log.d("Walid","formatCatSafeApplicativeData timestamp : $timestamp")
        result.append("timestamp : ${timestamp.formatFrameForLowBytesFirst()}\n")

        val commandCode = stringData.substring(startIndex = 64, endIndex = 68)
        // Log.d("Walid","formatCatSafeApplicativeData commandCode : $commandCode")
        result.append("commandCode : ${commandCode.formatFrameForLowBytesFirst()}\n")

        val applicativeDataLength = stringData.substring(startIndex = 68, endIndex = 72)
        // Log.d("Walid","formatCatSafeApplicativeData applicative data length : $applicativeDataLength")
        result.append("applicativeDataLength : ${applicativeDataLength.formatFrameForLowBytesFirst()}\n")

        val applicativeData = stringData.substring(startIndex = 72)

        // Log.d("Walid","formatCatSafeApplicativeData applicative data : $applicativeData")
        if (isLiveMessage((commandCode))) {
            // Log.d("Walid","formatCatSafeApplicativeData applicative data formated : ${formatCatSafeAppData(applicativeData)}")
            result.append("applicativeData : ${formatCatSafeAppData(applicativeData)}\n")
        } else {
            result.append(applicativeData)
        }


        return result.toString()
    }

    private fun formatCatSafeAppData(data: String): String {
        val result = StringBuilder()

        val parsedData = parseCatSafeAppData(data)

        for(key in parsedData.keys) {
            result.append("$key: ${parsedData[key]}\n")
        }

        return result.toString()
    }
}


val HEX_CHARS = "0123456789ABCDEF".toCharArray()


fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

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

fun String.formatFrameForHighBytesFirst() = chunked(2).reversed().joinToString("")
fun String.formatFrameForLowBytesFirst() = formatFrameForHighBytesFirst()
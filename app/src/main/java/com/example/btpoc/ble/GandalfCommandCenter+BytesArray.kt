package com.example.btpoc.ble

import android.util.Log

enum class FrameFormat(val length: Int) {
    LENGTH( 2),
    SOURCE_ID(6),
    SOURCE_PORT(2),
    DESTINATION_ID(6),
    DESTINATION_PORT(2),
    PRODUCT_ID(6),
    FRAME_COUNTER(4),
    TIMESTAMP(4),
    COMMAND_CODE(2),
    APP_DATA_LENGTH(2),
    APP_DATA(-1)
}


enum class ChannelIdBytes(val id: ByteArray, val length: Int) {
    DEVICE_CODED_STATUS(byteArrayOf(0x00, 0x5B), 1),
    ALARM_CODED_STATUS(byteArrayOf(0x00, 0x6B), 1),
    LAMP_CODE_STATUS(byteArrayOf(0x02, 0xC7.toByte()), 1),
    BATTERY_VOLTAGE(byteArrayOf(0x00, 0x23), 2),
    ACCELERATION_X(byteArrayOf(0x00, 0x29), 2),
    ACCELERATION_Y(byteArrayOf(0x00, 0x2A), 2),
    ACCELERATION_Z(byteArrayOf(0x00, 0x2B), 2),
    INCLINATION_X(byteArrayOf(0x00, 0x2C), 2),
    INCLINATION_Y(byteArrayOf(0x00, 0x2D), 2),
    INCLINATION_Z(byteArrayOf(0x00, 0x2E), 2),
    RSSI(byteArrayOf(0x00, 0x52), 2),
    BLE_STATUS(byteArrayOf(0x08, 0x98.toByte()), 1),
    SMART_MUTE_STATUS(byteArrayOf(0x0D, 0x16), 1),
    VOLTAGE_DETECTOR_STATUS(byteArrayOf(0x02, 0xCB.toByte()), 1),
    FORCED_ALARM_STATUS(byteArrayOf(0x02, 0xCC.toByte()), 1);

    companion object {
        fun fromByteArrayId(id: ByteArray): ChannelIdBytes? {
            return values().find { it.id.contentEquals(id) }
        }
    }
}


fun GandalfCommandCenter.formatCatSafeFrameToByteArray(data: ByteArray): ByteArray {
    val startTime = System.currentTimeMillis()
    val result = mutableListOf<Byte>()
    var index = 0

    for (format in FrameFormat.values()) {
        val length = format.length
        val segment: ByteArray

        if (length == -1) {
            // Handle the remaining data for APP_DATA
            segment = data.copyOfRange(index, data.size)
        } else {
            segment = data.copyOfRange(index, index + length).reversedArray()
            index += length
        }

        // Reverse the segment to correct for low-byte-first format
        result.addAll(segment.toList())

        //Log.d("Walid", "${format.name}: ${segment.toHex()}")
    }
    val endTime = System.currentTimeMillis()
    val timeTaken = endTime - startTime
    Log.d("Walid", "formatCatSafeFrameToByteArray in $timeTaken ms")

    return result.toByteArray()
}


fun GandalfCommandCenter.formatAppDataToByteArray(appData: ByteArray): ByteArray {
    val startTime = System.currentTimeMillis()
    val result = mutableListOf<Byte>()

    var index = 0
    while (index < appData.size) {
        // Extract the channel ID (2 bytes, low-byte-first format)
        val channelIdBytes = appData.copyOfRange(index, index + 2).reversedArray()
        index += 2

        // Find the corresponding ChannelId enum
        val channelId = ChannelIdBytes.fromByteArrayId(channelIdBytes)
        if (channelId != null) {
            // Extract the channel data based on the length defined in the enum
            val channelData = appData.copyOfRange(index, index + channelId.length).reversedArray()
            index += channelId.length

            // Add the channel ID and its data to the result
            result.addAll(channelIdBytes.toList())
            result.addAll(channelData.toList())

            //Log.d("Walid", "${channelId.name}: ${channelData.toHex()}")
        } else {
            Log.w("Walid", "Unknown channel ID: ${channelIdBytes.toHex()}")
            break // Stop processing if an unknown ID is encountered
        }
    }

    val endTime = System.currentTimeMillis()
    val timeTaken = endTime - startTime
    Log.d("Walid", "formatAppDataToByteArray in $timeTaken ms")

    return result.toByteArray()
}


fun GandalfCommandCenter.formatCatSafeFrameToMap(data: ByteArray): Map<FrameFormat, ByteArray> {
    val startTime = System.currentTimeMillis()
    Log.d("Walid", "formatCatSafeFrameToMap")
    val result = mutableMapOf<FrameFormat, ByteArray>()
    var index = 0

    for (format in FrameFormat.values()) {
        val length = format.length
        val segment: ByteArray

        if (length == -1) {
            // Handle APP_DATA as the remaining data
            // reversing APP_DATA will be done in the parsing method
            segment = data.copyOfRange(index, data.size)
        } else {
            // Reverse the segment to correct for low-byte-first format
            segment = data.copyOfRange(index, index + length).reversedArray()
            index += length
        }
        result[format] = segment

        //Log.d("Walid", "${format.name}: ${segment.toHex()}")
    }

    val endTime = System.currentTimeMillis()
    val timeTaken = endTime - startTime
    Log.d("Walid", "formatCatSafeFrameToMap in $timeTaken ms")

    return result
}

fun GandalfCommandCenter.formatAppDataToMap(appData: ByteArray): Map<ChannelIdBytes, ByteArray> {
    val startTime = System.currentTimeMillis()
    Log.d("Walid", "parseAppData")

    val result = mutableMapOf<ChannelIdBytes, ByteArray>()
    var index = 0

    while (index < appData.size) {
        // Extract the channel ID (2 bytes, low-byte-first format)
        val channelIdBytes = appData.copyOfRange(index, index + 2).reversedArray()
        index += 2

        // Find the corresponding ChannelId enum
        val channelId = ChannelIdBytes.fromByteArrayId(channelIdBytes)
        if (channelId != null) {
            // Extract the channel data based on the length defined in the enum
            val channelData = appData.copyOfRange(index, index + channelId.length).reversedArray()
            index += channelId.length
            result[channelId] = channelData

            //Log.d("Walid", "${channelId.name}: ${channelData.toHex()}")
        } else {
            Log.w("Walid", "Unknown channel ID: ${channelIdBytes.toHex()}")
            break // Stop processing if an unknown ID is encountered
        }
    }

    val endTime = System.currentTimeMillis()
    val timeTaken = endTime - startTime
    Log.d("Walid", "parseAppData end in : $timeTaken ms")

    return result
}


fun <K> Map<K, ByteArray>.toHexString(shouldPrintLine: Boolean = false): String {
    val result = StringBuilder()
    for ((key, value) in this) {
        result.append("$key => ${value.toHex()}   ")
        if (shouldPrintLine) result.append("\n")
    }
    return result.toString()
}
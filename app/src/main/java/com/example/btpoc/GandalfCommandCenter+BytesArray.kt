package com.example.btpoc

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
    val result = mutableListOf<Byte>()
    var index = 0

    for (format in FrameFormat.values()) {
        val length = format.length
        val segment: ByteArray

        if (length == -1) {
            // Handle the remaining data for APP_DATA
            segment = data.copyOfRange(index, data.size)
        } else {
            segment = data.copyOfRange(index, index + length)
            index += length
        }

        // Reverse the segment to correct for low-byte-first format
        val formattedSegment = segment.reversedArray()
        result.addAll(formattedSegment.toList())

        Log.d("Walid", "${format.name}: ${formattedSegment.toHex()}")
    }

    return result.toByteArray()
}


fun GandalfCommandCenter.formatCatSafeFrameToMap(data: ByteArray): Map<FrameFormat, ByteArray> {
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

        Log.d("Walid", "${format.name}: ${segment.toHex()}")
    }

    Log.d("Walid", "formatCatSafeFrameToMap end")
    return result
}

fun GandalfCommandCenter.parseAppData(appData: ByteArray): Map<ChannelIdBytes, ByteArray> {
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
            val channelData = appData.copyOfRange(index, index + channelId.length)
            index += channelId.length
            result[channelId] = channelData

            Log.d("Walid", "${channelId.name}: ${channelData.toHex()}")
        } else {
            Log.w("Walid", "Unknown channel ID: ${channelIdBytes.toHex()}")
            break // Stop processing if an unknown ID is encountered
        }
    }

    Log.d("Walid", "parseAppData end")

    return result
}


fun <K> Map<K, ByteArray>.toHexString(): String {
    val result = StringBuilder()
    for ((key, value) in this) {
        result.append("$key => ${value.toHex()}   ")
    }
    return result.toString()
}
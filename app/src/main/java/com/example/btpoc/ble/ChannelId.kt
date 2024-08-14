package com.example.btpoc.ble

import android.util.Log


const val LIVE_MESSAGE_TYPE = "0601"

enum class ChannelId(val id: String,val length: Int) {
    DEVICE_CODED_STATUS("005B", 1),
    ALARM_CODED_STATUS("006B", 1),
    LAMP_CODE_STATUS("02C7", 1),
    BATTERY_VOLTAGE("0023", 2),
    ACCELERATION_X("0029", 2),
    ACCELERATION_Y("002A", 2),
    ACCELERATION_Z("002B", 2),
    INCLINATION_X("002C", 2),
    INCLINATION_Y("002D", 2),
    INCLINATION_Z("002E", 2),
    RSSI("0052", 2),
    BLE_STATUS("0898", 1),
    SMART_MUTE_STATUS("0D16", 1),
    VOLTAGE_DETECTOR_STATUS("02CB", 1),
    FORCED_ALARM_STATUS("02CC", 1);

    companion object {
        fun fromStringId(id: String): ChannelId? {
            return values().find { it.id == id }
        }
    }
}

fun isLiveMessage(frame: String): Boolean {
    return frame.startsWith(LIVE_MESSAGE_TYPE.formatFrameForHighBytesFirst())
}

fun parseCatSafeAppData(data: String): Map<ChannelId, String> {
    val result = mutableMapOf<ChannelId, String>()
    var index = 0

    //Log.d("Walid", "parseCatSafeAppData : data = $data length = ${data.length}")

    while (index < data.length) {
        Log.d("Walid", "parseCatSafeAppData : index = $index")
        // Get the type (4 characters representing 2 bytes in the example)
        val type = data.substring(index, index + 4).formatFrameForLowBytesFirst()
        Log.d("Walid", "parseCatSafeAppData : type = $type")
        val dataType = ChannelId.fromStringId(id = type)
        Log.d("Walid", "parseCatSafeAppData : channelId = $dataType")

        if (dataType != null) {
            // Move the index past the type
            index += 4

            // Get the length of the data associated with this type
            val length = dataType.length * 2 // each byte is represented by 2 hex characters

            Log.d("Walid", "parseCatSafeAppData : channelId length = $length")

            // Get the data
            val value = data.substring(index, index + length)

            Log.d("Walid", "parseCatSafeAppData : channelId data value = $value")

            // Move the index past the value
            index += length

            // Store in the result map
            result[dataType] = value
        } else {
            // Handle unknown data type
            throw IllegalArgumentException("Unknown data type: $type")
        }
    }

    return result
}
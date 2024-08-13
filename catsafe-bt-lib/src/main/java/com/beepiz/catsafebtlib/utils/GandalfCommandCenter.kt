package com.beepiz.catsafebtlib.utils


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
}
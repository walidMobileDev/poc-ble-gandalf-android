package com.beepiz.catsafebtlib.utils



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
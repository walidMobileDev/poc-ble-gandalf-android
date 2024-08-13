package com.beepiz.catsafebtlib.bleconnection

import android.bluetooth.BluetoothProfile

class CatSafeException(errorType: CatSafeBLEError): Exception("error: $errorType")

enum class CatSafeBLEError {
    BLUETOOTH_NOT_ENABLED,
    CONNECTION_FAILED,
    SCAN_FAILED,
    DEVICE_DISCONNECTED,
    INVALID_STATE
}

enum class ConnectionState {
    STATE_DISCONNECTED,
    STATE_CONNECTING,
    STATE_CONNECTED,
    STATE_DISCONNECTING,
    STATE_SERVICE_DISCOVERED;

    companion object {
        fun stateFromIntValue(value: Int): ConnectionState {
            return when(value) {
                BluetoothProfile.STATE_DISCONNECTED -> STATE_DISCONNECTED
                BluetoothProfile.STATE_CONNECTING -> STATE_CONNECTING
                BluetoothProfile.STATE_CONNECTED -> STATE_CONNECTED
                BluetoothProfile.STATE_DISCONNECTING -> STATE_DISCONNECTING
                else -> throw CatSafeException(CatSafeBLEError.INVALID_STATE)
            }
        }
    }
}
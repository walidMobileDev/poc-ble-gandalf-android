package com.beepiz.catsafebtlib.communication

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

data class CatSafeBLEDevice(
    val name: String,
    val device: BluetoothDevice,
    val gatt: BluetoothGatt,
    val service: BluetoothGattService
)

var catSafeConnectedDevice: CatSafeBLEDevice? = null

val catSafeConnectedDeviceFlow = flow {
    while (true) {
        kotlinx.coroutines.delay(1000)
        emit(catSafeConnectedDevice)
    }
}
package com.beepiz.catsafebtlib.communication

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.flow.MutableSharedFlow

data class CatSafeBLEDevice(
    val name: String,
    val device: BluetoothDevice,
    val gatt: BluetoothGatt,
    val service: BluetoothGattService
)

val catSafeConnectedDeviceFlow = MutableSharedFlow<CatSafeBLEDevice?>()
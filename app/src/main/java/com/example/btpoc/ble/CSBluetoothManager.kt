package com.example.btpoc.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateListOf
import com.beepiz.catsafebtlib.CatSafeClient
import com.beepiz.catsafebtlib.bleconnection.ConnectionState
import com.beepiz.catsafebtlib.utils.toHex
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

val catSafeResponseDataFlow = MutableSharedFlow<ByteArray?>()

class CSBluetoothManager(context: Context) {
    private val catSafeClient = CatSafeClient(context)

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        catSafeClient.stopScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun isScanningFlow(): Flow<Boolean> {
        return flow {
            while (true) {
                emit(catSafeClient.adapter.scanning)
                delay(200)
            }
        }
    }

    val results = mutableStateListOf<BluetoothDevice>()


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun connectToDevice(device: BluetoothDevice) {
        Log.d("Walid", "connectToDevice : ${device.name}")
        bluetoothStateFlow.value = BluetoothConnectionState.Connecting
        catSafeClient.connectTo(device).collectLatest { state ->
            Log.d("Walid", "connectToDevice state: $state")

            if (state == ConnectionState.STATE_SERVICE_DISCOVERED) {
                bluetoothStateFlow.value = BluetoothConnectionState.Success
                sendCatSafeCommand()
            }

            if (state == ConnectionState.STATE_DISCONNECTED)
                bluetoothStateFlow.value = BluetoothConnectionState.Disconnected
        }
    }

    private suspend fun sendCatSafeCommand() {
        catSafeClient.sendCommand(GandalfCommandCenter.getVoltageLevelCommand()).collectLatest { response ->
            Log.d("Walid", "sendCatSafeCommand response: ${response.toHex()}")
            bluetoothStateFlow.emit(BluetoothConnectionState.DataAvailable)
            catSafeResponseDataFlow.emit(response)
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    suspend fun startScan() {
        results.removeAll { true }
        if (catSafeClient.adapter.scanning.not()) {
            catSafeClient.scan().collectLatest { device ->
                Log.d("Walid", "catSafeClient device scanned : ${device.name}")
                results.add(device)
            }
            bluetoothStateFlow.value = BluetoothConnectionState.Scanning
        } else {
            bluetoothStateFlow.value = BluetoothConnectionState.Initialized
            stopScan()
        }
    }
}
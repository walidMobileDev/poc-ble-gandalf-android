package com.beepiz.catsafebtlib

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.RequiresPermission
import com.beepiz.catsafebtlib.bleconnection.CatSafeBTAdapter
import com.beepiz.catsafebtlib.bleconnection.ConnectionState
import com.beepiz.catsafebtlib.communication.CatSafeCommander
import com.beepiz.catsafebtlib.utils.CSLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CatSafeClient(context: Context) {
    val adapter = CatSafeBTAdapter(context)

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun scan(deviceName: String? = null): Flow<BluetoothDevice> {
        return adapter.startScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        return adapter.stopScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectTo(device: BluetoothDevice): Flow<ConnectionState> {
        return adapter.connectToDevice(device)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendCommand(data: ByteArray): Flow<ByteArray> {
        CSLogger.debug(message = "sendCommand")
        CoroutineScope(Dispatchers.Default).launch {
            CSLogger.debug(message = "sendCommand on Default Dispatcher")
            CatSafeCommander.sendGandalfCommand(data)
        }

        return CatSafeCommander.catsafeResponseFlow
    }
}
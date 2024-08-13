package com.beepiz.catsafebtlib

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.RequiresPermission
import com.beepiz.catsafebtlib.bleconnection.CatSafeBTAdapter
import com.beepiz.catsafebtlib.bleconnection.ConnectionState
import com.beepiz.catsafebtlib.communication.CatSafeCommander
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class CatSafeClient(context: Context) {
    private val adapter = CatSafeBTAdapter(context)

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun scan(deviceName: String? = null): Flow<BluetoothDevice> {
        return adapter.startScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectTo(device: BluetoothDevice): Flow<ConnectionState> {
        return adapter.connectToDevice(device)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendCommand(): Flow<ByteArray> {
        CoroutineScope(Dispatchers.IO).launch {
            CatSafeCommander.sendGandalfCommand()
        }

        return CatSafeCommander.catsafeResponseFlow
    }
}
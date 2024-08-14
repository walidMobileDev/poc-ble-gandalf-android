package com.beepiz.catsafebtlib.bleconnection

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.annotation.RequiresPermission
import com.beepiz.catsafebtlib.utils.CSLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CatSafeBLEScanCallback : ScanCallback() {
    private val scannedDevices = mutableSetOf<BluetoothDevice>()

    private val _scannedDevicesFlow = MutableSharedFlow<BluetoothDevice>(replay = 1)
    val scannedDevicesFlow = _scannedDevicesFlow.asSharedFlow()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)
        result.device?.let { device ->
            if (scannedDevices.add(device)) {
                CSLogger.debug(message = "onScanResult : ${result.device.name}")
                _scannedDevicesFlow.tryEmit(device)
            }
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        throw CatSafeException(CatSafeBLEError.SCAN_FAILED)
    }

}
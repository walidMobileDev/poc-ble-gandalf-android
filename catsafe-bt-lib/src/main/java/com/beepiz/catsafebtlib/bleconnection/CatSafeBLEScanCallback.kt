package com.beepiz.catsafebtlib.bleconnection

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CatSafeBLEScanCallback : ScanCallback() {
    private val scannedDevices = mutableSetOf<BluetoothDevice>()

    private val _scannedDevicesFlow = MutableSharedFlow<BluetoothDevice>(replay = 0)
    val scannedDevicesFlow = _scannedDevicesFlow.asSharedFlow()


    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)
        Log.d("CatSafeLib","onScanResult : $result")
        result.device?.let { device ->
            if (scannedDevices.add(device)) {
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
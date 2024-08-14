package com.beepiz.catsafebtlib.bleconnection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.beepiz.catsafebtlib.utils.CSLogger
import kotlinx.coroutines.flow.SharedFlow

class CatSafeBTAdapter(private val context: Context) {
    companion object {
        val GANDALF_UUID = ParcelUuid.fromString("c991e030-812f-4eb5-a314-8b51a7754c39")!!
        val TX_CHARACTERISTIC = ParcelUuid.fromString("c991e031-812f-4eb5-a314-8b51a7754c39")!!
        val RX_CHARACTERISTIC = ParcelUuid.fromString("c991e032-812f-4eb5-a314-8b51a7754c39")!!
        val NOTIFICATION_DESCRIPTOR = ParcelUuid.fromString("00002902-0000-1000-8000-00805f9b34fb")!!
    }

    private val bluetoothAdapter: BluetoothAdapter
    private val bluetoothLeScanner: BluetoothLeScanner

    private val scanCallback = CatSafeBLEScanCallback()

    var scanning = false

    init {
        val service = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothAdapter = service?.adapter
            ?: throw CatSafeException(CatSafeBLEError.BLUETOOTH_NOT_ENABLED)

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan(forPeriodInSeconds: Long = 10000L): SharedFlow<BluetoothDevice> {
        if (scanning.not()) { // Stops scanning after a pre-defined scan period.
            Handler(Looper.myLooper() ?: Looper.getMainLooper()).postDelayed({
                scanning = false
                stopScan()
            }, forPeriodInSeconds)
            scanning = true
            val filters = listOf(createFilter())
            bluetoothLeScanner.startScan(filters, createScanSettings(), scanCallback)
        } else {
            scanning = false
            stopScan()
        }

        return scanCallback.scannedDevicesFlow
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        bluetoothLeScanner.stopScan(scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice): SharedFlow<ConnectionState> {
        CSLogger.debug(message = "connectToDevice : ${device.name}")
        val callback = CatSafeGattCallback()
        device.connectGatt(context, false, callback)

        return callback.connectionStateFlow
    }

    private fun createFilter(deviceAddress: String? = null) = ScanFilter.Builder()
        .setServiceUuid(GANDALF_UUID)
        .setDeviceAddress(deviceAddress)
        .build()

    private fun createScanSettings(): ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(0)
        .build()
}
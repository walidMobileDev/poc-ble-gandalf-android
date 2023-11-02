package com.example.btpoc

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.Serializable

//TODO flow and observing

val SERVICE_UUID = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb")!!

enum class BluetoothConnectionState {
    Initialized, Scanning, Connecting, DiscoveringServices, ReadingCharacteristics, Success, Disconnected
}

val bluetoothStateFlow = MutableStateFlow(BluetoothConnectionState.Initialized)

val services = mutableStateListOf<BluetoothGattService>()

class BluetoothManger(private val context: Context): Serializable {
    companion object {
        const val SCAN_PERIOD: Long = 10000
    }

    private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            bluetoothStateFlow.value = BluetoothConnectionState.Connecting
            Log.d("Walid","onConnectionStateChange newState : $newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
                bluetoothStateFlow.value = BluetoothConnectionState.DiscoveringServices
                // Device is connected, you can now discover services
                //TODO trigger event instead of switching activity here which is ew
                // here trigger event and in MainActivity collect and react
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bluetoothStateFlow.value = BluetoothConnectionState.Disconnected
            }
        }
        @SuppressLint("MissingPermission")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                context as MainActivity
                // Services discovered, you can now interact with the device
                context.lifecycleScope.launch {
                    Log.d("Walid","onServicesDiscovered : ${gatt?.services}")
                    gatt?.services?.forEach { service ->
                        Log.d("Walid","onServicesDiscovered service : ${service.uuid}")
                        if (services.contains(service).not()) services.add(service)
                        val characteristics = service.characteristics
                        gatt.readCharacteristic(characteristics[0])
                    }
                    bluetoothStateFlow.value = BluetoothConnectionState.Success
//                    if (result.not()) {
//                        val response = context.enableBluetoothAndAwaitResponse()
//                        if (response) gatt?.readCharacteristic(characteristic)
//                        else stopScan()
//                    }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            val statusString = if (status == BluetoothGatt.GATT_SUCCESS) "Success" else "oh no $status"
            if (status == BluetoothGatt.GATT_SUCCESS) bluetoothStateFlow.value = BluetoothConnectionState.Success
            Log.d("Walid","onCharacteristicRead : ${characteristic?.uuid} status : $statusString  value : ${characteristic?.value}")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            val statusString = if (status == BluetoothGatt.GATT_SUCCESS) "Success" else "oh no $status"
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d("Walid","onCharacteristicWrite : ${characteristic?.uuid} status : $statusString")
        }
    }

    private var scanCallback: ScanCallback? = null
    val results = mutableStateListOf<BluetoothDevice>()
    private var scanning = false
    private val handler = Handler()
    // Stops scanning after 10 seconds.

    init {
        scanCallback = object : ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                if (results.contains(result.device).not()) {
                    result.device?.let {
                        results.add(it)
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice) {
        device.connectGatt(context, false, gattCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothStateFlow.value = BluetoothConnectionState.Initialized
                stopScan()
            }, SCAN_PERIOD)
            scanning = true
            results.removeAll { true }
            //bluetoothLeScanner.startScan(scanCallback)
            bluetoothLeScanner.startScan(listOf(createFilter()), defaultBleScanSettings, scanCallback)
            bluetoothStateFlow.value = BluetoothConnectionState.Scanning
        } else {
            scanning = false
            bluetoothStateFlow.value = BluetoothConnectionState.Initialized
            stopScan()
        }
    }

    private fun createFilter(deviceAddress: String? = null): ScanFilter = ScanFilter.Builder()
        .setServiceUuid(SERVICE_UUID)
        .setDeviceAddress(deviceAddress)
        .build()

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        bluetoothLeScanner.stopScan(scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun isScanningFlow(): Flow<Boolean> {
        return flow {
            while (true) {
                emit(scanning)
                delay(200)
            }
        }
    }

    private val defaultBleScanSettings: ScanSettings = ScanSettings.Builder().also {
        it.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        it.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        it.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        it.setMatchMode(ScanSettings.MATCH_MODE_STICKY)
    }.build()
}
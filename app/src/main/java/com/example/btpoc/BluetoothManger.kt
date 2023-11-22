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
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.Serializable


val ACTION_POINT_UUID = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb")!!
val GANDALF_UUID = ParcelUuid.fromString("c991e030-812f-4eb5-a314-8b51a7754c39")!!
val GALAXY_BUDS_UUID = ParcelUuid.fromString("a7a473e9-19c6-491b-aea6-7ea92b8f043a")!!
val NOTIF_DESCRIPTOR = ParcelUuid.fromString("00002902-0000-1000-8000-00805f9b34fb")


enum class BluetoothConnectionState {
    Initialized, Scanning, Connecting, DiscoveringServices, ReadingCharacteristics, Success, DataAvailable, Disconnected
}

val bluetoothStateFlow = MutableStateFlow(BluetoothConnectionState.Initialized)
val servicesFlow = mutableStateListOf<BluetoothGattService>()
val characteristicFlow = mutableStateOf<BluetoothGattCharacteristic?>(null)//MutableStateFlow<BluetoothGattCharacteristic?>(null)

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
                    gatt?.services?.forEach { service ->
                        if (servicesFlow.contains(service).not()
                            && service.uuid.toString().startsWith("0000180").not())
                            servicesFlow.add(service)
                        else return@forEach
                        Log.d("Walid","onConnectionStateChange service : ${service.uuid}")
                        if (service.uuid == GANDALF_UUID.uuid) {
                            // Enable notifications for the Tx characteristic
                            sendGandalfCommand(gatt, service)
                            return@launch
                        }
                    }
                    bluetoothStateFlow.value = BluetoothConnectionState.Success
                    stopScan()
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
            if (status == BluetoothGatt.GATT_SUCCESS) CoroutineScope(Dispatchers.Main).launch {
                characteristicFlow.value = characteristic
                delay(1000)
                bluetoothStateFlow.emit(BluetoothConnectionState.DataAvailable)
            }
            Log.d("Walid","onCharacteristicRead : ${characteristic?.uuid} status : $statusString  value : ${characteristic?.value?.toHex()}")
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            val statusString = if (status == BluetoothGatt.GATT_SUCCESS) "Success" else "oh no $status"
            Log.d("Walid","onCharacteristicWrite : ${characteristic?.uuid} status : $statusString  value : ${characteristic?.value?.toHex()}")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d("Walid", "onCharacteristicChanged : ${characteristic.uuid}")
            if (characteristic.uuid == TX_CHARACTERISTIC.uuid) {
                // Process the response data
                val data = characteristic.value
                Log.d("Walid", "onCharacteristicChanged Received response: ${data.toHex()}")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.d("Walid", "onDescriptorWrite : ${descriptor?.uuid}")
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
                //Log.d("Walid","onScanResult : $result")
                if (results.contains(result.device).not()) {
                    result.device?.let {
                        results.add(it)
                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private suspend fun sendGandalfCommand(
        gatt: BluetoothGatt,
        service: BluetoothGattService
    ) {
        //stopScan()
        registerForNotifications(gatt, service)
        delay(1000)
        val rxCharacteristic = service.getCharacteristic(RX_CHARACTERISTIC.uuid)
        val command = GandalfCommandCenter.applicationRequestCommand()
        rxCharacteristic.value = command
        rxCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        Log.d("Walid", " sendBatteryStateCommand characteristic = ${rxCharacteristic.uuid} value = ${rxCharacteristic.value.toHex()}")
        gatt.writeCharacteristic(rxCharacteristic)
    }

    @SuppressLint("MissingPermission")
    private fun registerForNotifications(
        gatt: BluetoothGatt,
        service: BluetoothGattService
    ) {
        val txCharacteristic = service.getCharacteristic(TX_CHARACTERISTIC.uuid)
        val desc = txCharacteristic.getDescriptor(NOTIF_DESCRIPTOR.uuid)
        desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        val result =  gatt.writeDescriptor(desc)
        Log.d("Walid","registerForNotifications writeDescriptor result = $result")

        gatt.setCharacteristicNotification(txCharacteristic, true)
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
            val filters = createGandalfFilter()
            bluetoothLeScanner.startScan(scanCallback)
            //bluetoothLeScanner.startScan(filters, createGandalfScanSettings(), scanCallback)
            bluetoothStateFlow.value = BluetoothConnectionState.Scanning
        } else {
            scanning = false
            bluetoothStateFlow.value = BluetoothConnectionState.Initialized
            stopScan()
        }
    }

    private fun createFilter(deviceAddress: String? = null): ScanFilter = ScanFilter.Builder()
        .setServiceUuid(GANDALF_UUID)
        .setDeviceAddress(deviceAddress)
        .build()

    private fun createGandalfFilter(): List<ScanFilter> {
        val filters: MutableList<ScanFilter> = ArrayList()
        val filter = ScanFilter.Builder()
            .setServiceUuid(GANDALF_UUID)
            .build()
        filters.add(filter)
        return filters
    }

    private fun createGandalfScanSettings(): ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0)
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
        it.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        it.setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        it.setMatchMode(ScanSettings.MATCH_MODE_STICKY)
    }.build()
}
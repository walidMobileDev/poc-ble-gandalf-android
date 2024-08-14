package com.beepiz.catsafebtlib.bleconnection

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.annotation.RequiresPermission
import com.beepiz.catsafebtlib.bleconnection.CatSafeBTAdapter.Companion.GANDALF_UUID
import com.beepiz.catsafebtlib.bleconnection.CatSafeBTAdapter.Companion.TX_CHARACTERISTIC
import com.beepiz.catsafebtlib.communication.CatSafeBLEDevice
import com.beepiz.catsafebtlib.communication.CatSafeCommander
import com.beepiz.catsafebtlib.communication.catSafeConnectedDevice
import com.beepiz.catsafebtlib.communication.catSafeConnectedDeviceFlow
import com.beepiz.catsafebtlib.utils.CSLogger
import com.beepiz.catsafebtlib.utils.toHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CatSafeGattCallback : BluetoothGattCallback() {
    private val _connectionStateFlow = MutableSharedFlow<ConnectionState>(replay = 1)
    val connectionStateFlow = _connectionStateFlow.asSharedFlow()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        CSLogger.debug(message ="onConnectionStateChange newState : $newState")
        _connectionStateFlow.tryEmit(ConnectionState.stateFromIntValue(newState))
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt?.discoverServices()
        } else {
            CoroutineScope(Dispatchers.Default).launch {
                //catSafeConnectedDeviceFlow.emit(null)
            }
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            gatt?.getService(GANDALF_UUID.uuid)?.let { service ->
                CSLogger.debug(message ="onServicesDiscovered : ${service.uuid}")
                _connectionStateFlow.tryEmit(ConnectionState.STATE_SERVICE_DISCOVERED)
                catSafeConnectedDevice = CatSafeBLEDevice(gatt.device.name, gatt.device, gatt, service)
            }
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, value, status)
        val statusString = if (status == BluetoothGatt.GATT_SUCCESS) "Success" else "oh no $status"
        if (status == BluetoothGatt.GATT_SUCCESS)
            CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
        }
       CSLogger.debug(message =
            "onCharacteristicRead : ${characteristic.uuid} status : $statusString  value : ${value.toHex()}"
        )
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("onCharacteristicRead(gatt!!, characteristic!!, characteristic.value, status)")
    )
    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) = onCharacteristicRead(gatt!!, characteristic!!, characteristic.value, status)


    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        val statusString = if (status == BluetoothGatt.GATT_SUCCESS) "Success" else "oh no $status"
        CSLogger.debug(message =
            "onCharacteristicWrite : ${characteristic?.uuid} status : $statusString  value : ${characteristic?.value?.toHex()}"
        )
    }


    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        if (characteristic.uuid == TX_CHARACTERISTIC.uuid) {
            // Process the response data
            CSLogger.debug(message = "onCharacteristicChanged Received response: ${value.toHex()}")
            CoroutineScope(Dispatchers.Default).launch {
                /*bluetoothStateFlow.emit(BluetoothConnectionState.DataAvailable)
                delay(2000)
                characteristicFlow.emit(characteristic)*/
                CatSafeCommander.catsafeResponseFlow.emit(value)
            }
        }
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("onCharacteristicChanged(gatt, characteristic, characteristic.value)")
    )
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        if (characteristic.uuid == TX_CHARACTERISTIC.uuid) {
            val value = characteristic.value
            // Process the response data
            CSLogger.debug(message = "onCharacteristicChanged Received response: ${value.toHex()}")
            CoroutineScope(Dispatchers.Default).launch {
                /*bluetoothStateFlow.emit(BluetoothConnectionState.DataAvailable)
                delay(2000)
                characteristicFlow.emit(characteristic)*/
                CatSafeCommander.catsafeResponseFlow.emit(value)
            }
        }
    }
}
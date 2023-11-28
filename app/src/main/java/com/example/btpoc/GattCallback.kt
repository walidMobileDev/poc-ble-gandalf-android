package com.example.btpoc

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GattCallback(private val context: Context) : BluetoothGattCallback() {
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        bluetoothStateFlow.value = BluetoothConnectionState.Connecting
        Log.d("Walid", "onConnectionStateChange newState : $newState")
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
                    Log.d("Walid", "onConnectionStateChange service : ${service.uuid}")
                    if (service.uuid == GANDALF_UUID.uuid) {
                        // Enable notifications for the Tx characteristic
                        sendGandalfCommand(gatt, service)
                        return@launch
                    }
                }
                bluetoothStateFlow.value = BluetoothConnectionState.Success
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
        Log.d(
            "Walid",
            "onCharacteristicRead : ${characteristic?.uuid} status : $statusString  value : ${characteristic?.value?.toHex()}"
        )
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        val statusString = if (status == BluetoothGatt.GATT_SUCCESS) "Success" else "oh no $status"
        Log.d(
            "Walid",
            "onCharacteristicWrite : ${characteristic?.uuid} status : $statusString  value : ${characteristic?.value?.toHex()}"
        )
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
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


    @SuppressLint("MissingPermission")
    private suspend fun sendGandalfCommand(
        gatt: BluetoothGatt,
        service: BluetoothGattService
    ) {
        //stopScan()
        registerForNotifications(gatt, service)
        delay(1000)
        val rxCharacteristic = service.getCharacteristic(RX_CHARACTERISTIC.uuid)
        val command = GandalfCommandCenter.getFirmwareInfoCommand()
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
}
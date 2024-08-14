package com.beepiz.catsafebtlib.communication

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.util.Log
import androidx.annotation.RequiresPermission
import com.beepiz.catsafebtlib.bleconnection.CatSafeBLEError
import com.beepiz.catsafebtlib.bleconnection.CatSafeBTAdapter
import com.beepiz.catsafebtlib.bleconnection.CatSafeBTAdapter.Companion.RX_CHARACTERISTIC
import com.beepiz.catsafebtlib.bleconnection.CatSafeBTAdapter.Companion.TX_CHARACTERISTIC
import com.beepiz.catsafebtlib.bleconnection.CatSafeException
import com.beepiz.catsafebtlib.utils.CSLogger
import com.beepiz.catsafebtlib.utils.GandalfCommandCenter
import com.beepiz.catsafebtlib.utils.toHex
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.last

object CatSafeCommander {
    val catsafeResponseFlow = MutableSharedFlow<ByteArray>()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun sendGandalfCommand(command: ByteArray = GandalfCommandCenter.getVoltageLevelCommand()) {
        CSLogger.debug(message = "sendGandalfCommand : ${command.toHex()}")
        val gatt = catSafeConnectedDevice?.gatt
        val service = catSafeConnectedDevice?.service
        CSLogger.debug(message = "gatt : $gatt & service : ${service?.uuid}")
        //stopScan()
        if (gatt == null || service == null)
            throw CatSafeException(CatSafeBLEError.DEVICE_DISCONNECTED)
        registerForNotifications(gatt, service)
        delay(1000)
        val rxCharacteristic = service.getCharacteristic(RX_CHARACTERISTIC.uuid)
        rxCharacteristic?.value = command
        rxCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
       CSLogger.debug(
           message = "sendCommand characteristic = ${rxCharacteristic?.uuid} value = ${rxCharacteristic?.value?.toHex()}"
        )
        gatt.writeCharacteristic(rxCharacteristic)

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun registerForNotifications(gatt: BluetoothGatt, service: BluetoothGattService) {
        val txCharacteristic = service.getCharacteristic(TX_CHARACTERISTIC.uuid)
        val desc = txCharacteristic.getDescriptor(CatSafeBTAdapter.NOTIFICATION_DESCRIPTOR.uuid)
        desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(desc)

        gatt.setCharacteristicNotification(txCharacteristic, true)
    }
}
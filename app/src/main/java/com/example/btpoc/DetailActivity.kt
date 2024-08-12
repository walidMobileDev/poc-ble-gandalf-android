package com.example.btpoc

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.btpoc.ui.theme.BTPocTheme
import kotlinx.coroutines.launch

class DetailActivity: ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BTPocTheme {
                val state by bluetoothStateFlow.collectAsState(initial = BluetoothConnectionState.Initialized)

                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        characteristicFlow.collect { newCharacteristic ->
                            behaveAccordinglyTo(state, newCharacteristic)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (modifier = Modifier
                        .fillMaxWidth(fraction = 0.90F)
                        .verticalScroll(rememberScrollState())
                    ) {
                        DetailListTitle(
                            text = "Gatt services and characteristics",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .height(50.dp)
                        )
                        for (service in servicesFlow) {
                            CenteredText(
                                text = "services : ${service.uuid}",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .height(30.dp)
                            )
                            for (serviceCharacteristic in service.characteristics) {
                                CenteredSubText(
                                    text = "characteristic : ${serviceCharacteristic.uuid}",
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .height(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        bluetoothStateFlow.value = BluetoothConnectionState.Initialized
        servicesFlow.removeAll { true }
    }

    private fun behaveAccordinglyTo(status: BluetoothConnectionState, characteristic: BluetoothGattCharacteristic?) {
        characteristic?.value?.let { data ->
            val formattedData = GandalfCommandCenter.formatCatSafeFrameToMap(data)
            val formattedAppData = if (formattedData.containsKey(FrameFormat.APP_DATA))
                GandalfCommandCenter.formatAppDataToMap(formattedData.get(FrameFormat.APP_DATA)!!)
            else null

            val byteArrayFormattedData = GandalfCommandCenter.formatCatSafeFrameToByteArray(data)
            val byteArrayFormattedAppData = if (formattedData.containsKey(FrameFormat.APP_DATA))
                GandalfCommandCenter.formatAppDataToByteArray(formattedData.get(FrameFormat.APP_DATA)!!)
            else null

            val stringData = data.toHex()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Data Read")
            builder.setMessage(
                "Characteristic: ${characteristic.uuid}\n\n" +
                        //"Data Found: $stringData\n\n\n" +
                        "Formated Data: ${formattedData.toHexString(shouldPrintLine = true)}" +
                        "Formated App Data : ${formattedAppData?.toHexString(shouldPrintLine = true)}")
            builder.setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()

            Log.d("Walid", "Detail Activity => characterisitic read: ${characteristic.uuid} => data : ${data.toHex()}")
            Log.d("Walid", "Detail Activity => map formatted data : ${formattedData.toHexString()}")
            Log.d("Walid", "Detail Activity => map formatted appData : ${formattedAppData?.toHexString()}")

            Log.d("Walid", "Detail Activity => byte array formatted data : ${byteArrayFormattedData.toHexString()}")
            Log.d("Walid", "Detail Activity => byte array formatted appData : ${byteArrayFormattedAppData?.toHexString()}")
        }

        Toast.makeText(this,"new status : $status", Toast.LENGTH_SHORT).show()
    }
}
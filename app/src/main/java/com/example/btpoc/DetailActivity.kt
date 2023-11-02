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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.btpoc.ui.theme.BTPocTheme

class DetailActivity: ComponentActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BTPocTheme {
                val state = bluetoothStateFlow.collectAsState(initial = BluetoothConnectionState.Initialized).value
                val characteristic = characteristicFlow.value
                behaveAccordinglyTo(state, characteristic)
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
                            for (characteristic in service.characteristics) {
                                CenteredSubText(
                                    text = "characteristic : ${characteristic.uuid}",
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
            val string = data.contentToString()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Data Read")
            builder.setMessage("Characteristic: ${characteristic.uuid}\nData Found: $string")
            builder.setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }

        Log.d("Walid", "characterisitic read: ${characteristic?.uuid} => data : ${characteristic?.value.contentToString()}")
        Toast.makeText(this,"new status : $status", Toast.LENGTH_SHORT).show()
    }
}
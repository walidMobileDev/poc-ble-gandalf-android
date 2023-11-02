package com.example.btpoc

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.unit.dp
import com.example.btpoc.ui.theme.BTPocTheme

class DetailActivity: ComponentActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BTPocTheme {
                val state = bluetoothStateFlow.collectAsState(initial = BluetoothConnectionState.Initialized).value
                behaveAccordinglyTo(state)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (modifier = Modifier
                        .fillMaxWidth(fraction = 0.95F)
                        .verticalScroll(rememberScrollState())
                    ) {
                        DetailListTitle(
                            text = "Gatt services and characteristics",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .height(50.dp)
                        )
                        for (service in services) {
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
        bluetoothStateFlow.value = BluetoothConnectionState.ReadingCharacteristics
        services.removeAll { true }
    }

    private fun behaveAccordinglyTo(status: BluetoothConnectionState) {
        Toast.makeText(this,"new status : $status", Toast.LENGTH_SHORT).show()
    }
}
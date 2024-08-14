package com.example.btpoc

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.btpoc.ble.BluetoothConnectionState
import com.example.btpoc.ble.BluetoothManger
import com.example.btpoc.ble.CSBluetoothManager
import com.example.btpoc.ble.FrameFormat
import com.example.btpoc.ble.GandalfCommandCenter
import com.example.btpoc.ble.bluetoothStateFlow
import com.example.btpoc.ble.catSafeResponseDataFlow
import com.example.btpoc.ble.characteristicFlow
import com.example.btpoc.ble.formatAppDataToByteArray
import com.example.btpoc.ble.formatAppDataToMap
import com.example.btpoc.ble.formatCatSafeFrameToByteArray
import com.example.btpoc.ble.formatCatSafeFrameToMap
import com.example.btpoc.ble.toHex
import com.example.btpoc.ble.toHexString
import com.example.btpoc.ui.theme.BTPocTheme
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {
    private lateinit var bluetoothManager: BluetoothManger
    private lateinit var csBluetoothManager: CSBluetoothManager
    private lateinit var continuation: CancellableContinuation<Boolean>

    private val registration: ActivityResultLauncher<IntentSenderRequest> = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) {
        if (it.resultCode == RESULT_OK)
            bluetoothManager.startScan()
    }

    private val btEnablingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            continuation.resume(it.resultCode == RESULT_OK)
        }
    }

    private val btResultLauncher = registerForActivityResult(RequestMultiplePermissions()) { permissions ->
        permissions.forEach { permission ->
            if (permission.value.not())
                return@registerForActivityResult
        }
        lifecycleScope.launch { startScan() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //doesn't work with application context
        csBluetoothManager = CSBluetoothManager(context = this)
        bluetoothManager = BluetoothManger(context = this)
        setContent {
            BTPocTheme {
                val isScanning = csBluetoothManager.isScanningFlow().collectAsState(initial = false)
                val state = bluetoothStateFlow.collectAsState(initial = BluetoothConnectionState.Initialized).value

                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        catSafeResponseDataFlow.collect { data ->
                            behaveAccordinglyTo(state, data)
                        }
                    }
                }
                Log.d("Walid", "bluetoothStateFlow collectAsState : $state")
                behaveAccordinglyTo(status = state)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InitUi(
                        isScanning = isScanning.value,
                        csBluetoothScanner = csBluetoothManager,
                        results = csBluetoothManager.results
                    ) {
                       lifecycleScope.launch { startScan() }
                    }
                }
            }
        }
    }

    private fun behaveAccordinglyTo(status: BluetoothConnectionState) {
        Toast.makeText(this,"new status : $status", Toast.LENGTH_SHORT).show()
    }

    private fun behaveAccordinglyTo(status: BluetoothConnectionState, data: ByteArray?) {
        data?.let { safeData ->
            val formattedData = GandalfCommandCenter.formatCatSafeFrameToMap(safeData)
            val formattedAppData = if (formattedData.containsKey(FrameFormat.APP_DATA))
                GandalfCommandCenter.formatAppDataToMap(formattedData[FrameFormat.APP_DATA]!!)
            else null

            val byteArrayFormattedData = GandalfCommandCenter.formatCatSafeFrameToByteArray(safeData)
            val byteArrayFormattedAppData = if (formattedData.containsKey(FrameFormat.APP_DATA))
                GandalfCommandCenter.formatAppDataToByteArray(formattedData[FrameFormat.APP_DATA]!!)
            else null

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Data Read")
            builder.setMessage(
                        //"Data Found: $stringData\n\n\n" +
                        "Formated Data: ${formattedData.toHexString(shouldPrintLine = true)}" +
                        "Formated App Data : ${formattedAppData?.toHexString(shouldPrintLine = true)}")
            builder.setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()

            Log.d("Walid", "Detail Activity => response data : ${data.toHex()}")
            Log.d("Walid", "Detail Activity => map formatted data : ${formattedData.toHexString()}")
            Log.d("Walid", "Detail Activity => map formatted appData : ${formattedAppData?.toHexString()}")

            Log.d("Walid", "Detail Activity => byte array formatted data : ${byteArrayFormattedData.toHexString()}")
            Log.d("Walid", "Detail Activity => byte array formatted appData : ${byteArrayFormattedAppData?.toHexString()}")
        }

        Toast.makeText(this,"new status : $status", Toast.LENGTH_SHORT).show()
    }

    suspend fun enableBluetoothAndAwaitResponse(): Boolean {
        return if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            suspendCancellableCoroutine { continuation ->
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                btEnablingLauncher.launch(enableBtIntent)
                this@MainActivity.continuation = continuation
            }
        } else {
            false
        }
    }

    private suspend fun startScan() {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        ) {
            if (requestLocationAndAwaitResult()) csBluetoothManager.startScan()
            else Toast.makeText(this, "Please Enable Location", Toast.LENGTH_LONG).show()
        } else {
            btResultLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN
                ))
        }
    }


    private suspend fun requestLocationAndAwaitResult(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isLocationEnabled) return true

        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        return suspendCancellableCoroutine { continuation ->
            task.addOnSuccessListener {
                continuation.resume(locationManager.isLocationEnabled)
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        //exception.startResolutionForResult(this@MainActivity,
                            //REQUEST_CHECK_SETTINGS)
                        // This does not require onActivityResult that is deprecated
                        val request: IntentSenderRequest = IntentSenderRequest.Builder(
                            exception.resolution.intentSender
                        ).setFillInIntent(Intent())
                            .setFlags(FLAG_ACTIVITY_FORWARD_RESULT, 0)
                            .build()
                        registration.launch(request)
                    } finally {
                        continuation.resume(locationManager.isLocationEnabled)
                    }
                }
            }
        }
    }
}
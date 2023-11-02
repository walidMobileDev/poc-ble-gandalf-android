package com.example.btpoc

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
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
        bluetoothManager = BluetoothManger(context = this)
        setContent {
            BTPocTheme {
                val isScanning = bluetoothManager.isScanningFlow().collectAsState(initial = false)
                val state = bluetoothStateFlow.collectAsState(initial = BluetoothConnectionState.Initialized).value
                if (state == BluetoothConnectionState.Success) {
                    switchToDetailActivity()
                } else {
                    behaveAccordinglyTo(status = state)
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InitUi(
                        isScanning = isScanning.value,
                        bluetoothScanner = bluetoothManager,
                        results = bluetoothManager.results
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

    private fun switchToDetailActivity() {
        val intent = Intent(this, DetailActivity::class.java)
        startActivity(intent)
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
            if (requestLocationAndAwaitResult()) bluetoothManager.startScan()
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
package com.example.btpoc

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.btpoc.ui.theme.BTPocTheme

@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun InitUi(
    isScanning: Boolean,
    bluetoothScanner: BluetoothManger,
    results: SnapshotStateList<BluetoothDevice>,
    action: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        if (isScanning) {
            LinearProgressIndicator(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                color = Color.Cyan
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        CenteredText(
            isScanning,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(0.2F)
        )
        if (isScanning.not()) {
            Spacer(modifier = Modifier.height(20.dp))
            BluetoothScanButton(
                isScanning,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                action()
            }
        }
        if (results.size > 0)
            CenteredListTitle(
                isScanning,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
        //bluetoothScanner.result is a snapshotStateListe aka observable list yeaaay
        UpdateScanResult(
            results,
            bluetoothScanner = bluetoothScanner,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .align(Alignment.CenterHorizontally)
                .weight(1.0F)
        )
    }
}

@Composable
fun BluetoothScanButton(isScanning: Boolean, modifier: Modifier, action: (isScanning: Boolean) -> Unit) {
    Button(
        onClick = { action(isScanning) },
        colors = ButtonDefaults.buttonColors(Color.Cyan),
        enabled = isScanning.not(),
        modifier = modifier
    ) {
        val text = if (isScanning.not()) "Start Scan" else "Scanning ..."
        Text(text, color = Color.Black)
    }
}

@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun UpdateScanResult(
    results: SnapshotStateList<BluetoothDevice>,
    bluetoothScanner: BluetoothManger,
    modifier: Modifier
) {
    // Display the results of the Bluetooth LE scan.
    Column(modifier = modifier) {
        results.forEach {
            CellView(it, bluetoothScanner = bluetoothScanner)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CenteredListTitle(isScanResultEmpty: Boolean, modifier: Modifier) {
    val text = if (isScanResultEmpty) "Devices scanned : " else ""
    Text(text = text, modifier = modifier)
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun CellView(result: BluetoothDevice, bluetoothScanner: BluetoothManger) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        elevation = 10.dp,
        modifier = Modifier.clickable(onClick = {
            bluetoothScanner.connectToDevice(result)
        }),
        color = if (MaterialTheme.colors.isLight) MaterialTheme.colors.onSecondary else MaterialTheme.colors.background
    ) {
        Row(
            modifier = Modifier
                .padding(all = 8.dp)
                .border(1.5.dp, Color.Cyan, RoundedCornerShape(5))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bluetooth),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                CellTitle(result.name ?: "Unknown")
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    elevation = 1.dp,
                    color = MaterialTheme.colors.secondary,
                ) {
                    MessageText(result.address)
                }
            }
        }
    }
}

@Composable
fun CellTitle(string: String) {
    androidx.compose.material.Text(
        text = string,
        fontSize = 10.sp,
        color = Color.Cyan,
        style = MaterialTheme.typography.subtitle2
    )
}

@Composable
fun MessageText(string: String) {
    androidx.compose.material.Text(
        text = string,
        fontSize = 20.sp,
        style = MaterialTheme.typography.body2,
        color = MaterialTheme.colors.onSecondary,
        modifier = Modifier
            .padding(all = 4.dp)
    )
}

@Composable
fun CenteredText(isScanning: Boolean, modifier: Modifier) {
    val text = if (isScanning.not()) "Click to start the scan" else "Scanning ..."
    androidx.compose.material.Text(
        text = text,
        fontSize = 20.sp,
        color = Color.Cyan,
        style = MaterialTheme.typography.body2,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 1)
@Composable
fun MailPreview() {
    BTPocTheme {
        Column {
            BluetoothScanButton(
                false,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {}
            //CellView(message = Message(title = "Name of the device", body = "AE:OE:04:EB:A4:BF"))
        }
    }
}
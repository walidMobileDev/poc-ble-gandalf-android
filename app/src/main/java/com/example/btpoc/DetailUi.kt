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
fun DetailListTitle(text: String, modifier: Modifier) {
    Text(text = text, modifier = modifier)
}


@Composable
fun CenteredText(text: String, modifier: Modifier) {
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
private fun DetailPreview1() {
    BTPocTheme {
        Column {
            DetailListTitle(text = "Gatt Services :", modifier = Modifier.height(20.dp))
            //CellView(message = Message(title = "Name of the device", body = "AE:OE:04:EB:A4:BF"))
        }
    }
}
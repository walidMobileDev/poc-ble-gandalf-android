package com.example.btpoc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        fontSize =12.sp,
        color = Color.Cyan,
        style = MaterialTheme.typography.body1,
        modifier = modifier
    )
}

@Composable
fun CenteredSubText(text: String, modifier: Modifier) {
    androidx.compose.material.Text(
        text = text,
        fontSize = 10.sp,
        color = Color.Cyan,
        style = MaterialTheme.typography.caption,
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
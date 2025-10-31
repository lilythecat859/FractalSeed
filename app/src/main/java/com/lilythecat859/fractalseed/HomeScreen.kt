package com.lilythecat859.fractalseed

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var address by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var running by remember { mutableStateOf(false) }

    val vib = ContextCompat.getSystemService(ctx, android.os.Vibrator::class.java)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FractalSeed") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(R.string.hint_address)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (!isValidAddress(address)) {
                        Toast.makeText(ctx, "Invalid address", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    running = true
                    scope.launch {
                        val seed = address.lowercase().trim().toByteArray()
                        val bmp = FractalGenerator.generate(seed)
                        bitmap = bmp
                        saveImage(ctx, bmp, address)
                        vib?.vibrate(50)
                        running = false
                    }
                },
                enabled = !running
            ) {
                Text(stringResource(R.string.generate))
            }
            Spacer(Modifier.height(24.dp))
            if (running) {
                CircularProgressIndicator()
            }
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Fractal",
                    modifier = Modifier
                        .size(360.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                )
            }
        }
    }
}

private fun isValidAddress(a: String): Boolean =
    Pattern.matches("^(0x)?[0-9a-fA-F]{40}$", a)

private suspend fun saveImage(ctx: android.content.Context, bmp: Bitmap, addr: String) {
    withContext(Dispatchers.IO) {
        val name = "fractal_${addr.lowercase()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FractalSeed")
        }
        val uri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            ctx.contentResolver.openOutputStream(it)?.use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(ctx, R.string.saving, Toast.LENGTH_SHORT).show()
        }
    }
}

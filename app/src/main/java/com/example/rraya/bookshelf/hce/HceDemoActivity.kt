package com.example.rraya.bookshelf.hce

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.CRC32

class HceDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = PayloadStore(applicationContext)

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    HceDemoApp(store = store)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HceDemoApp(store: PayloadStore) {
    val activePayload by store.activePayloadFlow.collectAsStateWithLifecycle(initialValue = null)
    var tabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("HCE Demo (Safe Analog)") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Emulate") })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Payload") })
            }

            when (tabIndex) {
                0 -> EmulateScreen(activePayload = activePayload)
                1 -> PayloadEditorScreen(store = store, activePayload = activePayload)
            }
        }
    }
}

@Composable
private fun EmulateScreen(activePayload: ActivePayload?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Card (AID)", style = MaterialTheme.typography.titleMedium)
                SelectionContainer {
                    Text(bytesToHex(Apdu.AID), fontFamily = FontFamily.Monospace)
                }
                HorizontalDivider()
                Text("Active payload", style = MaterialTheme.typography.titleMedium)
                if (activePayload == null) {
                    Text("No active payload set. Go to Payload tab and save one.")
                } else {
                    Text("Name: ${activePayload.name}")
                    Text("Format: ${activePayload.format.displayName}")
                    Text("Size: ${activePayload.bytes.size} bytes")
                    Text("CRC32: ${crc32Hex(activePayload.bytes)}")
                }
            }
        }

        Text(
            "How to test: tap this phone on your ISO-DEP reader, SELECT the AID, " +
                "then call INS 0x10 (GET_INFO) and INS 0x20 (READ_CHUNK).",
        )
    }
}

@Composable
private fun PayloadEditorScreen(store: PayloadStore, activePayload: ActivePayload?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(activePayload?.name ?: "Demo payload") }
    var format by remember { mutableStateOf(activePayload?.format ?: PayloadFormat.JSON) }
    var inputText by remember { mutableStateOf(activePayload?.bytes?.toStringFor(format) ?: "") }
    var lastPickedUri by remember { mutableStateOf<Uri?>(null) }
    var lastPickedBytes by remember { mutableStateOf(activePayload?.bytes) }
    var error by remember { mutableStateOf<String?>(null) }
    var savedMsg by remember { mutableStateOf<String?>(null) }

    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        lastPickedUri = uri
        if (uri == null) return@rememberLauncherForActivityResult
        error = null
        savedMsg = null
        try {
            val bytes = context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "No input stream for $uri" }
                input.readBytes()
            }
            lastPickedBytes = bytes
        } catch (t: Throwable) {
            error = "Failed to read file: ${t.message}"
            lastPickedBytes = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Create / update active payload", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; savedMsg = null },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        FormatDropdown(
            value = format,
            onChange = {
                format = it
                savedMsg = null
                error = null
                // Reset editor view when switching formats
                inputText = ""
                lastPickedBytes = null
                lastPickedUri = null
            },
        )

        when (format) {
            PayloadFormat.RAW -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { openDocument.launch(arrayOf("*/*")) }) {
                        Text("Choose file")
                    }
                    if (lastPickedUri != null) {
                        Text("Selected")
                    }
                }
                if (lastPickedBytes != null) {
                    Text("Loaded: ${lastPickedBytes!!.size} bytes")
                } else {
                    Text("No file loaded.")
                }
            }

            PayloadFormat.HEX, PayloadFormat.BASE64, PayloadFormat.JSON -> {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it; savedMsg = null },
                    label = { Text("Input") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    error = null
                    savedMsg = null
                    val bytes = tryParseBytes(format, inputText, lastPickedBytes)
                        ?: run {
                            error = "No payload data."
                            return@Button
                        }
                    if (bytes.size > 8192) {
                        error = "Payload too large for this demo (${bytes.size} bytes). Try <= 8192."
                        return@Button
                    }
                    val payload = ActivePayload(
                        name = name.ifBlank { "Untitled" },
                        format = format,
                        bytes = bytes,
                    )
                    scope.launch {
                        store.setActivePayload(payload)
                        savedMsg = "Saved. Your reader can SELECT the AID and read the active payload."
                    }
                },
            ) {
                Text("Save")
            }
        }

        // Show derived info (best-effort)
        val previewBytes = tryParseBytes(format, inputText, lastPickedBytes)
        if (previewBytes != null) {
            Text("Preview size: ${previewBytes.size} bytes | CRC32: ${crc32Hex(previewBytes)}")
        }

        if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        }
        if (savedMsg != null) {
            Text(savedMsg!!)
        }
    }
}

@Composable
private fun FormatDropdown(value: PayloadFormat, onChange: (PayloadFormat) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text("Format", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Button(onClick = { expanded = true }) {
            Text(value.displayName)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PayloadFormat.entries.forEach { fmt ->
                DropdownMenuItem(
                    text = { Text(fmt.displayName) },
                    onClick = {
                        expanded = false
                        onChange(fmt)
                    },
                )
            }
        }
    }
}

private fun tryParseBytes(
    format: PayloadFormat,
    inputText: String,
    rawBytes: ByteArray?,
): ByteArray? {
    return when (format) {
        PayloadFormat.RAW -> rawBytes
        PayloadFormat.HEX -> parseHex(inputText)
        PayloadFormat.BASE64 -> parseBase64(inputText)
        PayloadFormat.JSON -> parseJsonUtf8(inputText)
    }
}

private fun parseHex(text: String): ByteArray? {
    val cleaned = text.replace(Regex("\\s+"), "")
    if (cleaned.isBlank()) return null
    if (cleaned.length % 2 != 0) throw IllegalArgumentException("Hex must have even length")
    val out = ByteArray(cleaned.length / 2)
    for (i in out.indices) {
        val idx = i * 2
        out[i] = cleaned.substring(idx, idx + 2).toInt(16).toByte()
    }
    return out
}

private fun parseBase64(text: String): ByteArray? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    return Base64.decode(trimmed, Base64.DEFAULT)
}

private fun parseJsonUtf8(text: String): ByteArray? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null

    // Basic validation: must parse as object or array
    if (trimmed.startsWith("[")) JSONArray(trimmed) else JSONObject(trimmed)
    return trimmed.toByteArray(Charsets.UTF_8)
}

private fun ByteArray.toStringFor(format: PayloadFormat): String {
    return when (format) {
        PayloadFormat.RAW -> ""
        PayloadFormat.HEX -> bytesToHex(this)
        PayloadFormat.BASE64 -> Base64.encodeToString(this, Base64.NO_WRAP)
        PayloadFormat.JSON -> toString(Charsets.UTF_8)
    }
}

private fun bytesToHex(bytes: ByteArray): String =
    bytes.joinToString(separator = "") { b -> "%02X".format(b) }

private fun crc32Hex(bytes: ByteArray): String {
    val v = CRC32().apply { update(bytes) }.value
    return "0x%08X".format(v)
}


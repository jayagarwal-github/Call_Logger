package com.calllogger

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.print.PrintAttributes.Margins
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.sql.Date



@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun CallLogScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val callLogs = remember { mutableStateListOf<String>() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            callLogs.clear()
            callLogs.addAll(getCallLogs(context))
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📞 Call Logger", fontWeight = FontWeight.Bold) }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val permission = Manifest.permission.READ_CALL_LOG
                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                callLogs.clear()
                                callLogs.addAll(getCallLogs(context))
                            } else {
                                permissionLauncher.launch(permission)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Read Logs")
                    }

                    Button(
                        onClick = {
                            sendAllCallLogsToPhpServer(context)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Send Logs")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (callLogs.isEmpty()) {
                    Text(
                        text = "No call logs available. Tap 'Read Logs' to load.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(callLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Text(
                                    text = log,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@SuppressLint("MissingPermission")
fun getCallLogs(context: Context): MutableList<String> {
    val result = mutableListOf<String>()
    val projection = arrayOf(
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION,
        CallLog.Calls.CACHED_NAME
    )
    val sortOrder = "${CallLog.Calls.DATE} DESC"

    context.contentResolver.query(
        CallLog.Calls.CONTENT_URI, projection, null, null, sortOrder
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            val type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
            val dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
            val duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)) ?: "Unknown"

            val date = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(dateMillis))
            val typeStr = when (type) {
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.MISSED_TYPE   -> "Missed"
                CallLog.Calls.REJECTED_TYPE -> "Rejected"
                else -> "Unknown"
            }

            val log = "$typeStr: $number ($name) on $date • ${duration}s"
            result.add(log)
        }
    }

    return result
}

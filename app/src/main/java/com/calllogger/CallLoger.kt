package com.calllogger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

fun getCallLogDataList(context: Context): List<CallLogData> {
    val result = mutableListOf<CallLogData>()
    val projection = arrayOf(
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION
    )
    val sortOrder = "${CallLog.Calls.DATE} DESC"

    context.contentResolver.query(
        CallLog.Calls.CONTENT_URI, projection, null, null, sortOrder
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            val typeInt = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
            val dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
            val duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))

            val type = when (typeInt) {
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.MISSED_TYPE -> "Missed"
                CallLog.Calls.REJECTED_TYPE -> "Rejected"
                else -> "Unknown"
            }

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateMillis))
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(dateMillis))
            val deviceNumber = getOwnPhoneNumber(context)

            result.add(
                CallLogData(
                    date, time, type, deviceNumber, number,
                    ring_duration = 0,
                    call_duration = duration.toInt()
                )
            )
        }
    }
    return result
}


@SuppressLint("MissingPermission")
fun getOwnPhoneNumber(context: Context): String {
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    return if (
        ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    ) {
        tm.line1Number ?: "Unavailable"
    } else {
        "Null"
    }
}


//Function used to send the Data on the php server
fun sendAllCallLogsToPhpServer(context: Context) {
    val allLogs = getCallLogDataList(context)

    if (allLogs.isEmpty()) {
        Toast.makeText(context, "No call logs found", Toast.LENGTH_SHORT).show()
        return
    }

    RetrofitClient.api.sendLogs(allLogs).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            Log.d("RETROFIT", "All logs sent: ${response.code()}")
            Toast.makeText(context, "All logs sent!", Toast.LENGTH_SHORT).show()
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("RETROFIT", "Error: ${t.message}")
            Toast.makeText(context, "Failed: ${t.message}", Toast.LENGTH_LONG).show()
        }
    })
}

package com.calllogger

data class CallLogData(
    val date: String,
    val time: String,
    val type: String,
    val device_number: String,
    val client_number: String,
    val ring_duration: Int,
    val call_duration: Int
)

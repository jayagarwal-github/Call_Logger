package com.calllogger

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface CallLogApi {
    @POST("call_log.php")
    fun sendLogs(@Body logs: List<CallLogData>): Call<Void>
}

package com.example.androidapprpg.utils



import java.text.SimpleDateFormat
import java.util.*


private val timeFmt = ThreadLocal.withInitial { SimpleDateFormat("HH:mm", Locale.getDefault()) }

fun formatTimeOrNow(millis: Long?): String {
    val value = millis ?: System.currentTimeMillis()
    return timeFmt.get().format(Date(value))
}
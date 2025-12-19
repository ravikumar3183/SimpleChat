package com.example.simplechat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// This is now a shared helper function available everywhere
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
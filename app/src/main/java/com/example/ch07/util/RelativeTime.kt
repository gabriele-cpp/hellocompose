package com.example.ch07.util

fun formatRelativeTime(now: Long, then: Long): String {
    val diff = (now - then).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    val month = 30 * day
    val year = 365 * day

    return when {
        diff < minute -> "baru saja"
        diff < hour -> "${diff / minute} menit lalu"
        diff < day -> "${diff / hour} jam lalu"
        diff < month -> "${diff / day} hari lalu"
        diff < year -> "${diff / month} bulan lalu"
        else -> "${diff / year} tahun lalu"
    }
}

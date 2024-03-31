package us.shoroa.smllk.utils

import okhttp3.internal.format
import java.time.Duration

class Timer {
    private val startTime = System.currentTimeMillis()
    fun sinceStart() = formattedMillis(System.currentTimeMillis())

    private fun formattedMillis(millis: Long) : String {
        val elapsed = millis - startTime
        val duration = Duration.ofMillis(elapsed)
        return format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())
    }
}
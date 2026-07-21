package dalbers.com.noise.playerscreen.model

data class TimerPreset(val hours: Int, val minutes: Int) {
    val millis: Long get() = (hours * 3600L + minutes * 60L) * 1000L

    val label: String get() = when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }

    val isCustom: Boolean get() = this !in standard

    companion object {
        val standard = listOf(
            TimerPreset(0, 15),
            TimerPreset(0, 30),
            TimerPreset(1, 0),
            TimerPreset(4, 0),
        )

        fun from(millis: Long): TimerPreset? {
            if (millis <= 0) return null
            val totalMinutes = (millis / 60000L).toInt()
            return TimerPreset(totalMinutes / 60, totalMinutes % 60)
        }
    }
}

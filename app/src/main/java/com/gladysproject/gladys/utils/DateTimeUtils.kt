package com.gladysproject.gladys.utils

import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    private val currentTimeZone: String
        get() {
            val tz = Calendar.getInstance().timeZone
            return tz.displayName
        }

    fun getCurrentDate(): String{
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    fun getRelativeTimeSpan(dateString: String): String {

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone(currentTimeZone)
        val date: Date
        return try {
            val now = System.currentTimeMillis()
            date = formatter.parse(dateString)

            DateUtils.getRelativeTimeSpanString(date.time, now, DateUtils.SECOND_IN_MILLIS).toString()

        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }

    }
}

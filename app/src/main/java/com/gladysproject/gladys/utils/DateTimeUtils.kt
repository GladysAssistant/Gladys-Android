package com.gladysproject.gladys.utils

import android.text.format.DateUtils
import java.lang.Exception
import java.text.DateFormat
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
        return DateFormat.getDateInstance(DateFormat.FULL).format(Date())
    }

    fun convertStringToDate(date: String): Date{
        return if (date == "" || date.isEmpty()) {
            // if the date is null or empty
            // return current date
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(getCurrentDate())
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(date)
        }
    }

    fun getDate(date: String): String {
        return try {
            DateFormat.getDateInstance(DateFormat.FULL).format(convertStringToDate(date))
        } catch (err: Exception){
            date
        }
    }

    fun getTime(date: String): String {
        return try {
            DateFormat.getTimeInstance(DateFormat.SHORT).format(convertStringToDate(date))
        } catch (err: Exception){
            date
        }
    }

    fun isAfterDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) {
            throw IllegalArgumentException("The dates must not be null")
        }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isAfterDay(cal1, cal2)
    }

    private fun isAfterDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        if (cal1 == null || cal2 == null) {
            throw IllegalArgumentException("The dates must not be null")
        }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return false
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return true
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return false
        return if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) true else cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR)
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

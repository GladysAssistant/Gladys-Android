package com.gladysproject.gladys.utils

import android.text.format.DateUtils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {

    private val currentTimeZone: String
        get() {
            val tz = Calendar.getInstance().timeZone
            return tz.displayName
        }

    fun parseDateTime(dateString: String): String {

        val formatter = SimpleDateFormat("dd/MM/yyyyHH:mm", Locale.FRANCE)
        val date: Date
        try {
            date = formatter.parse(dateString)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale("FRENCH"))

            return dateFormat.format(date)

        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

    }

    fun getRelativeTimeSpan(dateString: String): String {

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRENCH)
        formatter.timeZone = TimeZone.getTimeZone(currentTimeZone)
        val date: Date
        try {
            val now = System.currentTimeMillis()
            date = formatter.parse(dateString)

            return DateUtils.getRelativeTimeSpanString(date.time, now, DateUtils.SECOND_IN_MILLIS).toString()

        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

    }

    fun getDay(dayofweek: String): String {

        val date: String
        val days = arrayOf("Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi")
        date = Arrays.asList(*days)[Integer.parseInt(dayofweek)]

        return date
    }

    fun getIdDay(dayofweek: String): String {

        val date: String
        val days = arrayOf("Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi")
        val index = Arrays.asList(*days).indexOf(dayofweek)
        date = index.toString()

        return date
    }
}

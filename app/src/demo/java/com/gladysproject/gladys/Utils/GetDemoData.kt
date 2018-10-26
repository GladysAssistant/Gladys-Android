package com.gladysproject.gladys.Utils

import android.content.Context
import com.gladysproject.gladys.R
import com.gladysproject.gladys.database.entity.Event
import com.gladysproject.gladys.database.entity.Message
import com.gladysproject.gladys.database.entity.Rooms
import com.squareup.moshi.Moshi
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.Charsets.UTF_8

class GetDemoData {

    companion object {

        fun getHomeData(context: Context): MutableList<Rooms> {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter<Array<Rooms>>(Array<Rooms>::class.java)
            var devicetypesbyroom: Array<Rooms>? = null

            try {
                devicetypesbyroom = jsonAdapter.fromJson(getAssetsJSON(R.raw.devicetypesbyroom, context))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return devicetypesbyroom!!.toMutableList()
        }

        fun getTimelineData(context: Context): MutableList<Event> {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter<Array<Event>>(Array<Event>::class.java)
            var events: Array<Event>? = null

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val currentDate = Date()

            try {
                events = jsonAdapter.fromJson(getAssetsJSON(R.raw.events, context))
                events[0].datetime = dateFormat.format(currentDate.time - 1*3600*1000)
                events[1].datetime = dateFormat.format(currentDate.time - 1*3600*1000)
                events[2].datetime = dateFormat.format(currentDate.time - 3*3600*1000)
                events[3].datetime = dateFormat.format(currentDate.time - 6*3600*1000)
                events[4].datetime = dateFormat.format(currentDate.time - 6*3600*1000)
                events[5].datetime = dateFormat.format(currentDate.time - 8*3600*1000)
                events[6].datetime = dateFormat.format(currentDate.time - 9*3600*1000)
                events[7].datetime = dateFormat.format(currentDate.time - 16*3600*1000)
                events[8].datetime = dateFormat.format(currentDate.time - 17*3600*1000)
                events[9].datetime = dateFormat.format(currentDate.time - 19*3600*1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return events!!.toMutableList()
        }

        fun getChatData(context: Context): MutableList<Message> {
            val moshi = Moshi.Builder().build()
            val jsonAdapter = moshi.adapter<Array<Message>>(Array<Message>::class.java)
            var messages: Array<Message>? = null

            try {
                messages = jsonAdapter.fromJson(getAssetsJSON(R.raw.messages, context))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return messages!!.toMutableList()
        }

        private fun getAssetsJSON(fileName: Int, context: Context): String? {
            var json: String? = null
            try {
                val inputStream = context.resources.openRawResource(fileName)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, UTF_8)

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return json
        }
    }
}

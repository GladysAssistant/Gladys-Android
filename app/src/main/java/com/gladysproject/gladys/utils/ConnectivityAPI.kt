package com.gladysproject.gladys.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.preference.PreferenceManager
import android.util.Log
import io.socket.client.Socket
import io.socket.client.IO
import java.net.URISyntaxException

@Suppress("DEPRECATION")
class ConnectivityAPI {
    companion object {

        private fun getNetworkInfo(context: Context): NetworkInfo? {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo
        }

        private fun isConnected(context: Context): Boolean {
            return !(ConnectivityAPI.getNetworkInfo(context) == null || !ConnectivityAPI.getNetworkInfo(context)!!.isConnected)
        }

        private fun isConnectedWifi(context: Context): Boolean {
            val info = ConnectivityAPI.getNetworkInfo(context)
            return !(info == null || !info.isConnected || info.type != ConnectivityManager.TYPE_WIFI)
        }

        /**
         Get type of connection
         0 for no connection
         1 for local connection
         2 for external connection
        */
        fun getTypeOfConnection(context: Context): Int {
            return if (isConnected(context)) { if (isConnectedWifi(context)) { 1 } else { 2 } } else { 0 }
        }

        /**
         getLocalPreferences
         return string address
        */
        fun getLocalPreferences(context: Context) : String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return if(prefs.getString("local_ip", "fakeurl") != "") "http://${prefs.getString("local_ip", "fakeurl")}:${prefs.getString("local_port", "8080")}" else "http://fakeurl"
        }

        /**
         getNatPreferences
         return string address
         empty string if nat isn't active
         https address if https is active
         http address if not
        */
        fun getNatPreferences(context: Context) : String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return if (prefs.getBoolean("nat", false)){
                // Test if the string is not empty
                if(prefs.getString("dns", "fakeurl") != ""){
                    if (prefs.getBoolean("https", false)){
                        "https://${prefs.getString("dns", "fakeurl")}"
                    }else{
                        "http://${prefs.getString("dns", "fakeurl")}:${prefs.getString("nat_port", "80")}"
                    }
                }else {
                    "http://fakeurl"
                }
            }else{
                "http://fakeurl"
            }
        }

        /**
         Building the URL according to the type of connection
         */
        fun getUrl(context: Context): String {
            return when(getTypeOfConnection(context)){
                1 -> ConnectivityAPI.getLocalPreferences(context)
                2 -> ConnectivityAPI.getNatPreferences(context)
                else -> "http://noconnection"
            }
        }

        /**
         @return socket instance
         */
        object WebSocket {
            private var instance: Socket? = null

            fun getInstance(context: Context): Socket? {
                if (instance == null) {
                    try {
                        instance = IO.socket("${getUrl(context)}?__sails_io_sdk_version=0.13.7")
                    } catch (e: URISyntaxException) {
                        Log.e("Error", e.toString())
                    }
                }
                return instance
            }
        }
    }

}
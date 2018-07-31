package com.gladysproject.gladys.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.preference.PreferenceManager

@Suppress("DEPRECATION")
class Connectivity {
    companion object {

        private fun getNetworkInfo(context: Context): NetworkInfo? {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo
        }

        private fun isConnected(context: Context): Boolean {
            return !(Connectivity.getNetworkInfo(context) == null || !Connectivity.getNetworkInfo(context)!!.isConnected)
        }

        private fun isConnectedWifi(context: Context): Boolean {
            val info = Connectivity.getNetworkInfo(context)
            return !(info == null || !info.isConnected || info.type != ConnectivityManager.TYPE_WIFI)
        }

        /*
         Get type of connection
         0 for no connection
         1 for local connection
         2 for external connection
        */
        fun getTypeOfConnection(context: Context): Int {
            return if (isConnected(context)) { if (isConnectedWifi(context)) { 1 } else { 2 } } else { 0 }
        }

        /*
         getLocalPreferences
         return string address
        */
        fun getLocalPreferences(context: Context) : String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return "http:// ${prefs.getString("local_ip", "")}:${prefs.getString("local_port", "")}"
        }

        /*
         getNatPreferences
         return string address
         empty string if nat isn't active
         https address if https is active
         http address if not
        */
        fun getNatPreferences(context: Context) : String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return if (prefs.getBoolean("nat", false)){
                if (prefs.getBoolean("https", false)){
                    "https://${prefs.getString("dns", "")}"
                }else{
                    "http://${prefs.getString("dns", "")}:${prefs.getString("nat_port", "")}"
                }
            }else{
                ""
            }
        }
    }

}
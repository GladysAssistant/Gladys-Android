package com.gladysproject.gladys.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.preference.PreferenceManager
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
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
        private fun getLocalPreferences(context: Context) : String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            var portNb = prefs.getString("local_port", "8080")
            var httpOrHttps = "http"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                // for Android P and above, cleartext traffic are not allowed
                // see more at : https://android-developers.googleblog.com/2018/04/protecting-users-with-tls-by-default-in.html
                httpOrHttps = "https"
                //check port number just to be sure to not use default HTTP port...
                portNb = if (portNb == "8080" || portNb == "80") { "443" /*default port for HTTPS*/ } else { portNb }
            }

            val localIp = prefs.getString("local_ip", "fakeurl")

            return if(localIp != "") "$httpOrHttps://$localIp:$portNb/" else "http://fakeurl"
        }

        /**
         getNatPreferences
         return string address
         empty string if nat isn't active
         https address if https is active
         http address if not
        */
        private fun getNatPreferences(context: Context) : String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return if (prefs.getBoolean("nat", false)){
                // Test if the string is not empty
                if(prefs.getString("dns", "") != ""){
                    buildNatAddress(prefs.getString("dns", "fakeurl")!!, prefs.getString("nat_port", "")!!, prefs.getBoolean("https", false))
                }else {
                    "http://fakeurl"
                }
            }else{
                "http://fakeurl"
            }
        }

        private fun buildNatAddress(address : String, nat_port: String, https: Boolean): String{
            return if("https://" in address || "http://" in address){
                if(nat_port == "") {
                    "$address/"
                } else {
                    "$address/:$nat_port/"
                }
            } else {
                if (https && nat_port == ""){
                    "https://$address/"
                } else if (https && nat_port != ""){
                    "https://$address:$nat_port/"
                }else{
                    "http://$address:$nat_port/"
                }
            }
        }

        /**
         Building the URL according to the type of connection
         */
        fun getUrl(context: Context): String {
            val url = when(getTypeOfConnection(context)){
                1 -> ConnectivityAPI.getLocalPreferences(context)
                2 -> ConnectivityAPI.getNatPreferences(context)
                else -> "http://noconnection"
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P && url.startsWith("http://")) {
                // for Android P and above, cleartext traffic are not allowed
                // see more at : https://android-developers.googleblog.com/2018/04/protecting-users-with-tls-by-default-in.html
                url.replace("http://", "https://")
            }

            return url
        }

        /**
         @return socket instance
         */
        object WebSocket {
            private var instance: Socket? = null
            private var url: String = ""

            fun getInstance(context: Context): Socket? {

                if (instance == null || url != getUrl(context)) {
                    try {
                        val opts = IO.Options()
                        opts.callFactory = SelfSigningClientBuilder.unsafeOkHttpClient
                        opts.webSocketFactory = SelfSigningClientBuilder.unsafeOkHttpClient

                        url = getUrl(context)
                        instance = IO.socket("$url?__sails_io_sdk_version=0.13.7", opts)
                    } catch (e: URISyntaxException) {
                        Log.e("Error", e.toString())
                    }
                }
                return instance
            }
        }

        /** Mqtt API */
        class Mqtt(context: Context) {

            private var mqttAndroidClient: MqttAndroidClient = getMqttClient(context)
            private val subscriptionTopic = "sensor/+"

            init {
                mqttAndroidClient.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(b: Boolean, s: String) {}
                    override fun connectionLost(throwable: Throwable) {}
                    @Throws(Exception::class)
                    override fun messageArrived(topic: String, mqttMessage: MqttMessage) {}
                    override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {}
                })
                if(isMqttActivated(context) && mqttAndroidClient.serverURI != "noconnection") connect(context)
            }

            private fun isMqttActivated(context: Context): Boolean {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                return prefs.getBoolean("activate_mqtt", false)
            }

            private fun getMqttClient(context: Context): MqttAndroidClient {
                val mqttAndroidClient: MqttAndroidClient?
                mqttAndroidClient = when (getTypeOfConnection(context)) {
                    0 -> MqttAndroidClient(context, "noconnection", "")
                    else -> {
                        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                        MqttAndroidClient(context, "tcp://${prefs.getString("mqtt_host", "m49.cloudmqtt.com")}:${prefs.getString("mqtt_port", "168634")}", MqttClient.generateClientId())
                    }
                }
                return mqttAndroidClient
            }

            private fun getMqttOptions(context: Context): MqttConnectOptions {
                val mqttConnectOptions = MqttConnectOptions()
                mqttConnectOptions.isAutomaticReconnect = true
                mqttConnectOptions.isCleanSession = false

                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                if (prefs.getBoolean("activate_auth", false)) {
                    mqttConnectOptions.userName = prefs.getString("mqtt_user", "John")
                    mqttConnectOptions.password = prefs.getString("mqtt_user_password", "fifj6Hkdp")?.toCharArray()
                }

                return mqttConnectOptions
            }

            fun setCallback(callback: MqttCallbackExtended) {
                mqttAndroidClient.setCallback(callback)
            }

            private fun connect(context: Context) {
                val mqttConnectOptions = getMqttOptions(context)

                try {

                    mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            Log.e("Mqtt", "Connected !")

                            val disconnectedBufferOptions = DisconnectedBufferOptions()
                            disconnectedBufferOptions.isBufferEnabled = true
                            disconnectedBufferOptions.bufferSize = 100
                            disconnectedBufferOptions.isPersistBuffer = false
                            disconnectedBufferOptions.isDeleteOldestMessages = false
                            mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                            subscribeToTopic()
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                            Log.e("Mqtt", "Failed to connect to: " + exception.toString())
                        }
                    })


                } catch (ex: MqttException) {
                    Log.e("Mqtt", "Failed to connect to !")
                    Log.e("Mqtt", ex.message)
                }

            }

            private fun subscribeToTopic() {
                try {
                    mqttAndroidClient.subscribe(subscriptionTopic, 0, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            Log.e("Mqtt", "Subscribed !")
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                            Log.e("Mqtt", "Subscribed fail!")
                        }
                    })

                } catch (ex: MqttException) {
                    Log.e("Mqtt", "Subscribed fail !")
                    Log.e("Mqtt", ex.message)
                }
            }
        }
    }
}

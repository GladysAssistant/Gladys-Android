package com.gladysassistant.gladys.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.gladysassistant.gladys.utils.ConnectivityAPI
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttService : Service(){

    var mqttClient: ConnectivityAPI.Companion.Mqtt? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        try {
            mqttClient = ConnectivityAPI.Companion.Mqtt(applicationContext)
            mqttClient!!.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(b: Boolean, s: String) {}

                override fun connectionLost(throwable: Throwable) {}

                @Throws(Exception::class)
                override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                    Log.e("Debug", mqttMessage.toString())
                }

                override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {}
            })
        } catch (er: java.lang.Exception){}
    }

}
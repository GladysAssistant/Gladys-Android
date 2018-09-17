package com.gladysproject.gladys.utils

import com.gladysproject.gladys.models.DeviceTypeByRoom
import com.gladysproject.gladys.models.Event
import com.gladysproject.gladys.models.Message
import retrofit2.Call
import retrofit2.http.*


interface GladysAPI {

    @GET("/event?")
    fun getEvents(@Query("token") token: String): Call<MutableList<Event>>

    @GET("/event/create")
    fun createEvents(@Query("code") event: String, @Query("house") house_id: String, @Query("user") user_id: String, @Query("token") token: String): Call<Event>

    @GET("/devicetype/room")
    fun getDeviceTypeByRoom(@Query("token") token: String): Call<List<DeviceTypeByRoom>>

    @FormUrlEncoded
    @POST("/devicetype/{id}/exec")
    fun changeDeviceState(@Path("id") deviceType_id: Long?, @Field("value") value: Float?, @Field("token") token: String): Call<Void>

    @GET("/message/user/null")
    fun getMessages(@Query("token") token: String): Call<MutableList<Message>>

    @FormUrlEncoded
    @POST("/message")
    fun sendMessage(@Field("text") text: String?, @Field("receiver") receiver: Int?, @Field("token") token: String): Call<Message>

}


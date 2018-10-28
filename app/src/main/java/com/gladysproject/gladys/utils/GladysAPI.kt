package com.gladysproject.gladys.utils

import com.gladysproject.gladys.database.entity.Event
import com.gladysproject.gladys.database.entity.Message
import com.gladysproject.gladys.database.entity.Rooms
import com.gladysproject.gladys.models.Mode
import retrofit2.Call
import retrofit2.http.*

interface GladysAPI {

    /** Events */
    @GET("/eventtype")
    fun getEventsType(@Query("token") token: String): Call<MutableList<Event>>

    @GET("/event")
    fun getEvents(@Query("token") token: String): Call<MutableList<Event>>

    @GET("/event/create")
    fun createEvents(@Query("code") event: String, @Query("house") house_id: String, @Query("user") user_id: String, @Query("token") token: String): Call<Event>

    /** Devicetypes */

    @GET("/devicetype/room")
    fun getDeviceTypeByRoom(@Query("token") token: String): Call<MutableList<Rooms>>

    @FormUrlEncoded
    @POST("/devicetype/{id}/exec")
    fun changeDeviceState(@Path("id") deviceType_id: Long?, @Field("value") value: Float?, @Field("token") token: String): Call<Void>

    /** Messages */

    @GET("/message/user/null")
    fun getMessages(@Query("token") token: String): Call<MutableList<Message>>

    @FormUrlEncoded
    @POST("/message")
    fun sendMessage(@Field("text") text: String?, @Field("receiver") receiver: Int?, @Field("token") token: String): Call<Message>

    /** Modes */

    @GET("/mode")
    fun getModes(@Query("token") token: String): Call<MutableList<Mode>>

    @FormUrlEncoded
    @POST("/house/{id}/mode")
    fun changeMode(@Path("id") house_id: Long?, @Field("mode") mode: String?, @Field("token") token: String): Call<Void>
}


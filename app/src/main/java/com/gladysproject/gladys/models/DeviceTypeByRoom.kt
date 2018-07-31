package com.gladysproject.gladys.models

import com.squareup.moshi.Json
import java.util.ArrayList

class DeviceTypeByRoom {

    @Json(name = "name")
    var roomName: String? = null

    @Json(name = "id")
    var roomId: Long = 0

    var deviceTypes: List<DeviceType> = ArrayList()
    
    var isExpanded: Boolean = false

    constructor(){}

    constructor(roomName: String, roomId: Long, deviceTypes: List<DeviceType>, isExpanded: Boolean) : this() {
        this.roomName = roomName
        this.roomId = roomId
        this.deviceTypes = deviceTypes
        this.isExpanded = isExpanded
    }

}


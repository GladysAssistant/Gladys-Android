package com.gladysproject.gladys.database.entity

import com.squareup.moshi.Json

class DeviceType {

    @Json(name = "name")
    var deviceTypeName: String? = null

    @Json(name = "id")
    var deviceTypeId: Long = 0

    var type: String? = null
    var category: String? = null
    var tag: String? = null
    var unit: String? = null
    var min: Int? = null
    var max: Int? = null
    var display: Long? = null
    var sensor: Long? = null
    var lastChanged: String? = null
    var lastValue: Float? = null
    var roomId: Long? = null
    var roomName: String? = null

    constructor() {}

    constructor(deviceTypeName: String, deviceTypeId: Long, type: String, category: String, tag: String, unit: String, min: Int?, max: Int?, display: Long?, sensor: Long?, lastChanged: String, lastValue: Float?, roomId: Long?, roomName: String) : super() {
        this.deviceTypeName = deviceTypeName
        this.deviceTypeId = deviceTypeId
        this.type = type
        this.category = category
        this.tag = tag
        this.unit = unit
        this.min = min
        this.max = max
        this.display = display
        this.sensor = sensor
        this.lastChanged = lastChanged
        this.lastValue = lastValue
        this.roomId = roomId
        this.roomName = roomName
    }
}


package com.gladysproject.gladys.database.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName="devicetype")
open class DeviceType {

    @PrimaryKey(autoGenerate = false)
    var id: Long = 0
    var deviceTypeName: String? = null
    var type: String? = null
    var category: String? = null
    var tag: String? = null
    var unit: String? = null
    var min: Int? = null
    var max: Int? = null
    var sensor: Long? = null
    var lastValue: Float? = null
    var roomId: Long? = null
    var roomHouse: Long? = null

    @Ignore
    var display: Long? = 1

    constructor()

    constructor(deviceTypeName: String, id: Long, type: String, category: String, tag: String, unit: String, min: Int?, max: Int?, display: Long?, sensor: Long?, lastValue: Float?, roomId: Long?, roomHouse: Long?) : super() {
        this.deviceTypeName = deviceTypeName
        this.id = id
        this.type = type
        this.category = category
        this.tag = tag
        this.unit = unit
        this.min = min
        this.max = max
        this.display = display
        this.sensor = sensor
        this.lastValue = lastValue
        this.roomId = roomId
        this.roomHouse = roomHouse
    }
}


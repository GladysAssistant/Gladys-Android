package com.gladysassistant.gladys.database.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName="room")
open class Rooms{

    @PrimaryKey(autoGenerate = false)
    var id: Long = 0
    var name: String? = null
    var house: String? = null
    var isExpanded: Boolean = false

    @Ignore
    var deviceTypes: MutableList<DeviceType> = ArrayList()

    constructor()

    constructor(name: String, house:String, id: Long, isExpanded: Boolean, deviceTypes: MutableList<DeviceType>) {
        this.name = name
        this.house = house
        this.id = id
        this.isExpanded = isExpanded
        this.deviceTypes = deviceTypes
    }
}


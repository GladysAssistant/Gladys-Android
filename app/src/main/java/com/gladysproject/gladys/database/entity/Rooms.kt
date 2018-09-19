package com.gladysproject.gladys.database.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName="room")
open class Rooms{

    @PrimaryKey(autoGenerate = false)
    var id: Long = 0
    var name: String? = null
    var isExpanded: Boolean = false

    @Ignore
    var deviceTypes: MutableList<DeviceType> = ArrayList()

    constructor()

    constructor(name: String, id: Long, isExpanded: Boolean, deviceTypes: MutableList<DeviceType>) {
        this.name = name
        this.id = id
        this.isExpanded = isExpanded
        this.deviceTypes = deviceTypes
    }
}


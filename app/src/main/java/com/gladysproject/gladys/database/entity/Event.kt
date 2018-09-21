package com.gladysproject.gladys.database.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName="event")
open class Event{

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var name: String? = null
    var code: String? = null
    var datetime: String? = null
    var user: Int? = null

    constructor()

    @Ignore
    constructor(name: String, code: String, datetime: String, user: Int?) {
        this.name = name
        this.code = code
        this.datetime = datetime
        this.user = user
    }
}


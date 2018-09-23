package com.gladysproject.gladys.database.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName="task")
open class Task{

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var name: String? = null
    var triggerType: String? = null
    var triggerParam: String? = null
    var actionType: String? = null
    var actionParam: String? = null

    constructor()

    @Ignore
    constructor(name: String, triggerType: String, triggerParam: String, actionType: String, actionParam: String?) {
        this.name = name
        this.triggerType = triggerType
        this.triggerParam = triggerParam
        this.actionType = actionType
        this.actionParam = actionParam
    }
}


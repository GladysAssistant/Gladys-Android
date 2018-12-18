package com.gladysproject.gladys.database.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName="message")
open class Message{

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var text: String? = null
    var sender: Int? = null
    var senderName: String? = null
    var datetime: String? = null
    var receiver: Int? = null

    constructor()

    @Ignore
    constructor(text: String, sender: Int, senderName: String, datetime: String, receiver: Int?) {
        this.text = text
        this.sender = sender
        this.senderName = senderName
        this.datetime = datetime
        this.receiver = receiver
    }
}


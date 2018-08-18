package com.gladysproject.gladys.models

open class Message{

    var text: String? = null
    var sender: Int? = null
    var senderName: String? = null
    var datetime: String? = null
    var receiver: Int? = null

    constructor() {}

    constructor(text: String, sender: Int, senderName: String, datetime: String, receiver: Int?) {
        this.text = text
        this.sender = sender
        this.senderName = senderName
        this.datetime = datetime
        this.receiver = receiver
    }
}


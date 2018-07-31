package com.gladysproject.gladys.models

class Event{

    var name: String? = null
    var code: String? = null
    var datetime: String? = null
    var user: Int? = null

    constructor() {}

    constructor(name: String, code: String, datetime: String, user: Int?) {
        this.name = name
        this.code = code
        this.datetime = datetime
        this.user = user
    }
}


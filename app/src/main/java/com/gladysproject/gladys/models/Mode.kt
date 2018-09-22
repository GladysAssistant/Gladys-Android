package com.gladysproject.gladys.models

class Mode {

    lateinit var name: String
    lateinit var code: String
    var id: Long? = 1

    constructor() {}

    constructor(name: String, code: String, id: Long?) {
        this.name = name
        this.code = code
        this.id = id
    }

}
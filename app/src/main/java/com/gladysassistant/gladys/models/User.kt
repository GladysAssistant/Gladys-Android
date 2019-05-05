package com.gladysassistant.gladys.models

class User {

    lateinit var firstname: String
    lateinit var lastName: String
    var userId: Long? = 1

    constructor() {}

    constructor(firstname: String, lastName: String, userId: Long?) {
        this.firstname = firstname
        this.lastName = lastName
        this.userId = userId
    }

}

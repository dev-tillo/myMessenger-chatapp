package com.example.mymessenger.myClasses

import com.google.gson.reflect.TypeToken
import java.io.Serializable

class Account : Serializable {
    var email: String? = null
    var displayName: String? = null
    var photoUrl: String? = null
    var uid: String? = null
    var colorMessage: String? = null
//    var token: String? = null

    constructor(
        email: String?,
        displayName: String?,
        photoUrl: String?,
        uid: String?,
        colorMessage: String,
//        token: String,
    ) {
        this.email = email
        this.displayName = displayName
        this.photoUrl = photoUrl
        this.uid = uid
        this.colorMessage = colorMessage
//        this.token = token
    }

    constructor()
}

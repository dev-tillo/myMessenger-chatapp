package com.example.mymessenger.keraksiz.notification

class Data {

    private var user: String? = null
    private var icon: Int? = null
    private var body: String? = null
    private var title: String? = null
    private var sented: String? = null

    constructor(user: String?, icon: Int?, body: String?, title: String?, sented: String?) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.sented = sented
    }

    constructor()
}
package com.example.mymessenger.keraksiz.notification

class Sender {

    var data: Data? = null
    var to: String? = null

    constructor(data: Data?, to: String?) {
        this.data = data
        this.to = to
    }
}
package edu.put.inf151872_2

class Upload {
    var imageUrl: String? = null
    var name: String? = null
    var key: String? = null

    constructor() {
        // Empty constructor needed for Firebase
    }

    constructor(name: String?, imageUrl: String?) {
        this.name = name
        this.imageUrl = imageUrl
    }
}

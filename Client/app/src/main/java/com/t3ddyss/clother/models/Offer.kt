package com.t3ddyss.clother.models

data class Offer(val title: String,
                 val address: String,
                 val image: String) {
    var id = 0 // To exclude this property from equals() method
}

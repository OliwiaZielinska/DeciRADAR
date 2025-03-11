package com.example.deciradar.CloudFirestore

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("surname") @set:PropertyName("surname") var surname: String = "",
    @get:PropertyName("login") @set:PropertyName("login") var login: String = "",
    @get:PropertyName("password") @set:PropertyName("password") var password: String = "",
    @get:PropertyName("endNightMode") @set:PropertyName("endNightMode") var endNightMode: String = "",
    @get:PropertyName("startNightMode") @set:PropertyName("startNightMode") var startNightMode: String = "",
)
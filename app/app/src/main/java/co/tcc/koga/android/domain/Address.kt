package co.tcc.koga.android.domain

import java.io.Serializable

class Address(
    var locality: String,
    var state: String,
    var country: String
): Serializable
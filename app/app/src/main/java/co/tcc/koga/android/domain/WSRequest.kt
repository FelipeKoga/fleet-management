package co.tcc.koga.android.domain

import java.io.Serializable

data class WSRequest(
    var action: String,
    var data: String
): Serializable

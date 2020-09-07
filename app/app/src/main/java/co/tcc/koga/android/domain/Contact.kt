package co.tcc.koga.android.domain

import java.io.Serializable

data class Contact(
    var name: String,
    var email: String,
    var companyId: String? = "",
    var id: String? = ""
): Serializable
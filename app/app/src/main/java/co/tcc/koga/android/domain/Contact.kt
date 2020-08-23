package co.tcc.koga.android.domain

import java.io.Serializable

data class Contact(
    var name: String,
    var status: ContactStatus,
    var photo: String? = "",
    var company: String? = "",
    var id: String? = "",
    var isGroup: Boolean = false
): Serializable
package co.tcc.koga.android.domain

import java.io.Serializable

data class User(
    val username: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val companyId: String,
    val avatar: String?,
    var isSelected: Boolean = false
): Serializable
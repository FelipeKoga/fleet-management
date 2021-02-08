package co.tcc.koga.android.dto

import java.io.Serializable

data class UserDTO(
    val username: String,
    val name: String,
    val phone: String,
    val email: String,
    val companyId: Int
): Serializable
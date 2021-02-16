package co.tcc.koga.android.data.network.payload

import java.io.Serializable

data class LocationPayload(
    val latitude: Double,
    val longitude: Double,
    val username: String,
    val companyId: String
): Serializable
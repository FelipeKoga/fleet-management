package co.tcc.koga.android.data.network.payload

import java.io.Serializable

data class UploadResponse(
    val getURL: String,
    val putURL: String
): Serializable
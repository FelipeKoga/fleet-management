package co.tcc.koga.android.data.network.payload

import java.io.Serializable

data class UploadUrlPayload(
    val key: String,
    val bucket: String = "tcc-project-assets"
): Serializable